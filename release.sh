#!/bin/bash
set -e

function clean {
    ARG=$?
	echo "=> Exit status = $ARG"
	pkill -f 'java.*arlas-server' || echo "no arlas-server running"
	rm arlas.log
	rm pom.xml.versionsBackup
	git checkout -- .
    exit $ARG
}
trap clean EXIT

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

ELASTIC_VERSIONS_5=("5.0.2" "5.1.2" "5.2.2" "5.3.3" "5.4.3" "5.5.1")
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

echo "=> Package arlas-server"
mvn clean package

echo "=> Start arlas-server"
# stop already running server
pkill -f 'java.*arlas-server' || echo "no arlas-server running"
# start fresh new server
cp target/arlas-server-${VERSION}.jar target/arlas-server-${VERSION}-release.jar
java -jar target/arlas-server-${VERSION}-release.jar server conf/configuration.yaml > arlas.log 2>&1 &

echo "=> Wait for arlas-server up and running"
i=1; until nc -w 2 localhost 9999; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done

itests() {
	echo "=> Run integration tests with several elasticsearch versions (${ELASTIC_VERSIONS[*]})"
	for i in "${ELASTIC_RANGE[@]}"
    do
	    ./scripts/tests-integration.sh --es=$i
    done
}
if [ "$TESTS" == "YES" ]; then itests; else echo "=> Skip integration tests"; fi

echo "=> Generate documentation"
mkdir tmp || echo "tmp exists"
curl -XGET http://localhost:9999/arlas/swagger.json -o tmp/swagger.json
curl -XGET http://localhost:9999/arlas/swagger.yaml -o tmp/swagger.yaml
mvn  swagger2markup:convertSwagger2markup post-integration-test

echo "=> Generate client APIs"
swagger-codegen generate  -i tmp/swagger.json  -l typescript-angular2 -o tmp/typescript-angular2


echo "=> Build Typescript API "${FULL_API_VERSION}
BASEDIR=$PWD
cd ${BASEDIR}/tmp/typescript-angular2/
cp ${BASEDIR}/conf/npm/package-build.json package.json
cp ${BASEDIR}/conf/npm/tsconfig-build.json .
npm version --no-git-tag-version ${FULL_API_VERSION}
npm install
npm run build-release
cd ${BASEDIR}

echo "=> Publish Typescript API "
cp ${BASEDIR}/conf/npm/package-publish.json ${BASEDIR}/tmp/typescript-angular2/dist/package.json
cd ${BASEDIR}/tmp/typescript-angular2/dist
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
