#!/bin/bash
set -e

npmlogin=`npm whoami`
if  [ -z "$npmlogin"  ] ; then echo "your are not logged on npm"; exit -1; else  echo "logged as "$npmlogin ; fi

function clean_docker {
    echo "===> stop arlas-server stack"
    docker-compose --project-name arlas down
}

function clean_exit {
    ARG=$?
	echo "=> Exit status = $ARG"
	rm pom.xml.versionsBackup
	rm -rf target/tmp || echo "target/tmp already removed"
	git checkout -- .
	clean_docker
    exit $ARG
}
trap clean_exit EXIT

usage(){
	echo "Usage: ./release.sh -api=X -es=Y -rel=Z -dev=Z+1 [--no-tests]"
	echo " -es |--elastic-range           elasticsearch versions supported"
	echo " -api-major|--api-version       release arlas-server API major version"
	echo " -api-minor|--api-minor-version release arlas-server API minor version"
	echo " -api-patch|--api-patch-version release arlas-server API patch version"
	echo " -rel|--arlas-release        release arlas-server version"
	echo " -dev|--arlas-dev            development arlas-server version (-SNAPSHOT qualifier will be automatically added)"
	echo " --no-tests                     do not run integration tests"
	exit 1
}

TESTS="YES"
for i in "$@"
do
case $i in
    -rel=*|--arlas-release=*)
    ARLAS_REL="${i#*=}"
    shift # past argument=value
    ;;
    -dev=*|--arlas-dev=*)
    ARLAS_DEV="${i#*=}"
    shift # past argument=value
    ;;
    -api-major=*|--api-major-version=*)
    API_MAJOR_VERSION="${i#*=}"
    shift # past argument=value
    ;;
    -api-minor=*|--api-minor-version=*)
    API_MINOR_VERSION="${i#*=}"
    shift # past argument=value
    ;;
    -api-patch=*|--api-patch-version=*)
    API_PATCH_VERSION="${i#*=}"
    shift # past argument=value
    ;;
    -es=*|--elastic-range=*)
    ELASTIC_RANGE="${i#*=}"
    shift # past argument=value
    ;;
    --no-tests)
    TESTS="NO"
    shift # past argument with no value
    ;;
    *)
            # unknown option
    ;;
esac
done

ELASTIC_VERSIONS_5=("5.0.2" "5.1.2" "5.2.2" "5.3.3" "5.4.3" "5.5.1" "5.6.5")
case $ELASTIC_RANGE in
    "5")
        ELASTIC_VERSIONS=( "${ELASTIC_VERSIONS_5[@]}" )
        ;;
    *)
        echo "Unknown --elasticsearch-range value"
        echo "Possible values : "
        echo "   -es=5 for versions ${ELASTIC_VERSIONS_5[*]}"
        usage
esac


if [ -z ${ELASTIC_VERSIONS+x} ]; then usage;else echo "Elasticsearch versions support : ${ELASTIC_VERSIONS[*]}"; fi
if [ -z ${API_MAJOR_VERSION+x} ]; then usage;  else    echo "API MAJOR version        : ${API_MAJOR_VERSION}"; fi
if [ -z ${API_MINOR_VERSION+x} ]; then usage;  else    echo "API MINOR version        : ${API_MINOR_VERSION}"; fi
if [ -z ${API_PATCH_VERSION+x} ]; then usage;  else    echo "API PATCH version        : ${API_PATCH_VERSION}"; fi
if [ -z ${ARLAS_REL+x} ]; then usage;    else    echo "Release version                : ${ARLAS_REL}"; fi
if [ -z ${ARLAS_DEV+x} ]; then usage;    else    echo "Next development version       : ${ARLAS_DEV}"; fi
                                                 echo "Running tests                  : ${TESTS}"

VERSION="${API_MAJOR_VERSION}.${ELASTIC_RANGE}.${ARLAS_REL}"
DEV="${API_MAJOR_VERSION}.${ELASTIC_RANGE}.${ARLAS_DEV}"
FULL_API_VERSION=${API_MAJOR_VERSION}"."${API_MINOR_VERSION}"."${API_PATCH_VERSION}

echo "=> Get develop branch"
git checkout develop
git pull origin develop

echo "=> Update project version"
mvn versions:set -DnewVersion=${VERSION}

sed -i.bak 's/\"API_VERSION\"/\"'${FULL_API_VERSION}'\"/' src/main/java/io/arlas/server/rest/explore/ExploreRESTServices.java

docker build --tag arlas-server:${VERSION} --tag arlas-server:latest --tag gisaia/arlas-server:${VERSION} --tag gisaia/arlas-server:latest .
docker push gisaia/arlas-server:${VERSION}
docker push gisaia/arlas-server:latest

echo "=> Start arlas-server stack"
export ARLAS_VERSION=${VERSION}
export ELASTIC_VERSION="5.6.5"
docker-compose --project-name arlas up -d
DOCKER_IP=$(docker-machine ip || echo "localhost")

echo "=> Wait for arlas-server up and running"
i=1; until nc -w 2 ${DOCKER_IP} 19999; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done

echo "=> Get swagger documentation"
mkdir target/tmp || echo "target/tmp exists"
i=1; until curl -XGET http://${DOCKER_IP}:19999/arlas/swagger.json -o target/tmp/swagger.json; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done
i=1; until curl -XGET http://${DOCKER_IP}:19999/arlas/swagger.yaml -o target/tmp/swagger.yaml; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done


echo "=> Stop arlas-server stack"
docker-compose --project-name arlas down

itests() {
	echo "=> Run integration tests with several elasticsearch versions (${ELASTIC_VERSIONS[*]})"
	for i in "${ELASTIC_RANGE[@]}"
    do
	    ./scripts/tests-integration.sh --es=$i
    done
}
if [ "$TESTS" == "YES" ]; then itests; else echo "=> Skip integration tests"; fi

echo "=> Generate client APIs"
BASEDIR=$PWD
#@see scripts/build-swagger-codegen.sh if you need a fresher version of swagger codegen
docker run --rm \
	-v $PWD:/opt/gen \
	-v $HOME/.m2:/root/.m2 \
	gisaia/swagger-codegen:2.2.3

echo "=> Build Typescript API "${FULL_API_VERSION}
cd ${BASEDIR}/target/tmp/typescript-angular2/
cp ${BASEDIR}/conf/npm/package-build.json package.json
cp ${BASEDIR}/conf/npm/tsconfig-build.json .
npm version --no-git-tag-version ${FULL_API_VERSION}
npm install
npm run build-release
cd ${BASEDIR}

echo "=> Publish Typescript API "
cp ${BASEDIR}/conf/npm/package-publish.json ${BASEDIR}/target/tmp/typescript-angular2/dist/package.json
cd ${BASEDIR}/target/tmp/typescript-angular2/dist
npm version --no-git-tag-version ${FULL_API_VERSION}

npm publish || echo "Publishing on npm failed ... continue ..."
cd ${BASEDIR}

echo "=> Commit release version"
git commit -a -m "release version ${VERSION}"
git tag v${VERSION}
git push origin v${VERSION}
git push origin develop

echo "=> Merge develop into master"
git checkout master
git pull origin master
git merge origin/develop
git push origin master

echo "=> Update project version for develop"
git checkout develop
git pull origin develop
git rebase origin/master
mvn versions:set -DnewVersion=${DEV}-SNAPSHOT
echo "=> Update REST API version in JAVA source code"
sed -i.bak 's/\"'${FULL_API_VERSION}'\"/\"API_VERSION\"/' src/main/java/io/arlas/server/rest/explore/ExploreRESTServices.java
git commit -a -m "development version ${DEV}-SNAPSHOT"
git push origin develop
