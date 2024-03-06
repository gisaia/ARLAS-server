#!/bin/bash
set -o errexit -o pipefail
SCRIPT_DIRECTORY="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"
PROJECT_ROOT_DIRECTORY="$SCRIPT_DIRECTORY"

npmlogin=`npm whoami`
if  [ -z "$npmlogin"  ] ; then echo "your are not logged on npm"; exit -1; else  echo "logged as "$npmlogin ; fi

function clean_docker {
    echo "===> stop arlas-server stack"
    docker-compose -f docker-compose.yml -f docker-compose-elasticsearch.yml --project-name arlas down -v
}

function clean_exit {
    ARG=$?
	echo "=> Exit status = $ARG"
	rm -rf pom.xml.versionsBackup
	rm -rf target/tmp || echo "target/tmp already removed"
	clean_docker
	if [ "$SIMULATE" == "NO" ]; then
        git checkout -- .
        mvn clean
    else
        echo "=> Skip discard changes";
        git checkout -- pom.xml
        sed -i.bak 's/\"'${FULL_API_VERSION}'\"/\"API_VERSION\"/' arlas-rest/src/main/java/io/arlas/server/rest/explore/ExploreRESTServices.java
    fi
    exit $ARG
}
trap clean_exit EXIT

usage(){
	echo "Usage: ./release.sh -api=X -es=Y -rel=Z -dev=Z+1 [--no-tests] [--skip-api]"
	echo " -es |--elastic-range           elasticsearch versions supported"
	echo " -api-major|--api-version       release arlas-server API major version"
	echo " -api-minor|--api-minor-version release arlas-server API minor version"
	echo " -api-patch|--api-patch-version release arlas-server API patch version"
	echo " -rel|--arlas-release           release arlas-server version"
	echo " -dev|--arlas-dev               development arlas-server version (-SNAPSHOT qualifier will be automatically added)"
	echo " --no-tests                     do not run integration tests"
	echo " --simulate                     do not publish artifacts and git push local branches"
	echo " --skip-api                     do not generate clients APIs"
	exit 1
}

TESTS="YES"
SIMULATE="NO"
SKIP_API="NO"
BASEDIR=$PWD
for i in "$@"
do
case $i in
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
    --no-tests)
    TESTS="NO"
    shift # past argument with no value
    ;;
    --simulate)
    SIMULATE="YES"
    shift # past argument with no value
    ;;
    --skip-api)
    SKIP_API="YES"
    shift # past argument with no value
    ;;
    *)
            # unknown option
    ;;
esac
done

ELASTIC_VERSIONS_7=(
  7.2.1
  7.3.2
  7.4.2
  7.5.2
  7.6.2
  7.7.1
  7.8.1
  7.9.2
  7.12.1
  7.14.2
  7.15.2
  7.16.0
  7.17.6
)

ELASTIC_VERSIONS=( "${ELASTIC_VERSIONS_7[@]}" )

if [ -z ${ELASTIC_VERSIONS+x} ]; then usage;   else echo "Elasticsearch versions support : ${ELASTIC_VERSIONS[*]}"; fi
if [ -z ${API_MAJOR_VERSION+x} ]; then usage;  else    echo "API MAJOR version           : ${API_MAJOR_VERSION}"; fi
if [ -z ${API_MINOR_VERSION+x} ]; then usage;  else    echo "API MINOR version           : ${API_MINOR_VERSION}"; fi
if [ -z ${API_PATCH_VERSION+x} ]; then usage;  else    echo "API PATCH version           : ${API_PATCH_VERSION}"; fi
if [ -z ${ARLAS_DEV+x} ]; then usage;          else    echo "Next development version    : ${ARLAS_DEV}"; fi
                                                       echo "Running tests               : ${TESTS}"
                                                       echo "Simulate mode               : ${SIMULATE}"

if [ "$SIMULATE" == "NO" -a "$SKIP_API" == "NO" ]; then
    if  [ -z "$PIP_LOGIN"  ] ; then echo "Please set PIP_LOGIN environment variable"; exit -1; fi
    if  [ -z "$PIP_PASSWORD"  ] ; then echo "Please set PIP_PASSWORD environment variable"; exit -1; fi
    if  [ -z "$GITHUB_CHANGELOG_TOKEN"  ] ; then echo "Please set GITHUB_CHANGELOG_TOKEN environment variable"; exit -1; fi
    if  [ -z "$CLOUDSMITH_LOGIN"  ] ; then echo "Please set CLOUDSMITH_LOGIN environment variable"; exit -1; fi
    if  [ -z "$CLOUDSMITH_API_KEY"  ] ; then echo "Please set CLOUDSMITH_API_KEY environment variable"; exit -1; fi
fi


export ARLAS_VERSION="${API_MAJOR_VERSION}.${API_MINOR_VERSION}.${API_PATCH_VERSION}"
ARLAS_DEV_VERSION="${API_MAJOR_VERSION}.${API_MINOR_VERSION}.${ARLAS_DEV}"
FULL_API_VERSION=${API_MAJOR_VERSION}"."${API_MINOR_VERSION}"."${API_PATCH_VERSION}
API_DEV_VERSION=${API_MAJOR_VERSION}"."${API_MINOR_VERSION}"."${ARLAS_DEV}

echo "Release : ${ARLAS_VERSION}"
echo "API     : ${FULL_API_VERSION}"
echo "Dev     : ${ARLAS_DEV_VERSION}"

echo "=> Get develop branch"
if [ "$SIMULATE" == "NO" ]; then
    git checkout develop
    git pull origin develop
else echo "=> Skip develop checkout"; fi

echo "=> Update project version"
mvn clean
mvn versions:set -DnewVersion=${ARLAS_VERSION}
sed -i.bak 's/\"API_VERSION\"/\"'${FULL_API_VERSION}'\"/' arlas-rest/src/main/java/io/arlas/server/rest/explore/ExploreRESTServices.java
sed -i.bak 's/^appVersion: .*$/appVersion: '${ARLAS_VERSION}'/' packaging/helm/arlas-server/Chart.yaml
sed -i.bak 's/^version: .*$/version: '${ARLAS_VERSION}'/' packaging/helm/arlas-server/Chart.yaml

if [ "$SIMULATE" == "NO" ]; then
    export DOCKERFILE="Dockerfile"
else
    echo "=> Build arlas-server"
    docker run \
        -e GROUP_ID="$(id -g)" \
        -e USER_ID="$(id -u)" \
        --mount dst=/mnt/.m2,src="$HOME/.m2/",type=bind \
        --mount dst=/opt/maven,src="$PWD",type=bind \
        -w /opt/maven \
        --rm \
        maven:3.8.5-openjdk-17 \
            mvn clean install
fi

echo "=> Start arlas-server stack"
export ARLAS_SERVICE_RASTER_TILES_ENABLE=true
export ELASTIC_DATADIR="/tmp"
docker-compose -f docker-compose-elasticsearch.yml --project-name arlas up -d
echo "Waiting for ES readiness"
docker run --net arlas_default --rm busybox sh -c 'i=1; until nc -w 2 elasticsearch 9200; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'
echo "ES is ready"
docker-compose -f docker-compose.yml --project-name arlas up -d --build
DOCKER_IP=$(docker-machine ip || echo "localhost")

echo "=> Wait for arlas-server up and running"
i=1; until nc -w 2 ${DOCKER_IP} 19999; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done

echo "=> Get swagger documentation"
mkdir -p target/tmp || echo "target/tmp exists"
i=1; until curl -XGET http://${DOCKER_IP}:19999/arlas/openapi.json -o target/tmp/openapi.json; do if [ $i -lt 60 ]; then sleep 1; else break; fi; i=$(($i + 1)); done
i=1; until curl -XGET http://${DOCKER_IP}:19999/arlas/openapi.yaml -o target/tmp/openapi.yaml; do if [ $i -lt 60 ]; then sleep 1; else break; fi; i=$(($i + 1)); done

mkdir -p openapi
cp target/tmp/openapi.yaml openapi
cp target/tmp/openapi.json openapi

echo "=> Generate API documentation"
mvn "-Dswagger.output=docs/api" swagger2markup:convertSwagger2markup

echo "=> Stop arlas-server stack"
docker-compose -f docker-compose.yml -f docker-compose-elasticsearch.yml --project-name arlas down -v

itests() {
	echo "=> Run integration tests with several elasticsearch versions (${ELASTIC_VERSIONS[*]})"
	for i in "${ELASTIC_VERSIONS[@]}"
    do
	    ./scripts/tests-integration.sh --es=$i
    done
}
if [ "$TESTS" == "YES" ]; then itests; else echo "=> Skip integration tests"; fi

if [ "$SKIP_API" == "YES" ]; then
  echo "=> Skipping generation of API clients"
else
    echo "=> Generate client APIs"
    mkdir -p target/tmp/java-api
    docker run --rm \
        -e GROUP_ID="$(id -g)" \
        -e USER_ID="$(id -u)" \
        --mount dst=/input/api.json,src="$PWD/target/tmp/openapi.json",type=bind,ro \
        --mount dst=/input/config.json,src="$PWD/conf/swagger/java-config.json",type=bind,ro \
        --mount dst=/output,src="$PWD/target/tmp/java-api",type=bind \
        gisaia/swagger-codegen-3.0.42 \
            -l java --type-mappings GeoJsonObject=Object

    mkdir -p target/tmp/typescript-fetch
    docker run --rm \
        -e GROUP_ID="$(id -g)" \
        -e USER_ID="$(id -u)" \
        --mount dst=/input/api.json,src="$PWD/target/tmp/openapi.json",type=bind,ro \
        --mount dst=/output,src="$PWD/target/tmp/typescript-fetch",type=bind \
        gisaia/swagger-codegen-3.0.42 \
            -l typescript-fetch --additional-properties modelPropertyNaming=snake_case

    echo "=> Build Typescript API "${FULL_API_VERSION}
    cd ${BASEDIR}/target/tmp/typescript-fetch/
    cp ${BASEDIR}/conf/npm/package-build.json package.json
    cp ${BASEDIR}/conf/npm/tsconfig-build.json .
    npm version --no-git-tag-version ${FULL_API_VERSION}
    npm install
    npm run build-release
    npm run postbuild
    cd ${BASEDIR}

    echo "=> Publish Typescript API "
    cp ${BASEDIR}/conf/npm/package-publish.json ${BASEDIR}/target/tmp/typescript-fetch/dist/package.json
    cd ${BASEDIR}/target/tmp/typescript-fetch/dist
    npm version --no-git-tag-version ${FULL_API_VERSION}

    if [ "$SIMULATE" == "NO" ]; then
        npm publish || echo "Publishing on npm failed ... continue ..."
    else echo "=> Skip npm api publish"; fi
fi

cd ${BASEDIR}

if [ "$SIMULATE" == "NO" ]; then
    echo "=> Tag arlas-server docker image"
    docker tag gisaia/arlas-server:${ARLAS_VERSION} gisaia/arlas-server:latest
    echo "=> Push arlas-server docker image"
    docker push gisaia/arlas-server:${ARLAS_VERSION}
    docker push gisaia/arlas-server:latest
else echo "=> Skip docker push image"; fi


if [ "$SIMULATE" == "NO" ]; then
    echo "=> Publish jars in Maven cloudsmith repo"
    # publish the parent jar
    mvn -N -s ${BASEDIR}/conf/maven/settings.xml deploy
    # publish arlas-commons jar
    cd ${BASEDIR}/arlas-commons
    mvn -s ${BASEDIR}/conf/maven/settings.xml deploy
    # publish arlas-core jar
    cd ${BASEDIR}/arlas-core
    mvn -s ${BASEDIR}/conf/maven/settings.xml deploy
    # publish arlas-admin jar
    cd ${BASEDIR}/arlas-admin
    mvn -s ${BASEDIR}/conf/maven/settings.xml deploy
    # publish arlas-server-client jar
    if [[ "$SKIP_API" == "NO" ]]; then
        cd ${BASEDIR}/target/tmp/java-api
        cp ${BASEDIR}/conf/maven/pom.xml ${BASEDIR}/target/tmp/java-api/pom.xml
        mvn versions:set -DnewVersion=${ARLAS_VERSION}
        mvn -s ${BASEDIR}/conf/maven/settings.xml deploy
    else
         echo "=> Skipping publishing arlas-server-client jar"
    fi;
    cd ${BASEDIR}
else echo "=> Skip pushing jars in maven repo"; fi

if [ "$SIMULATE" == "NO" ]; then
    echo "=> Generate CHANGELOG.md"
    git tag v${ARLAS_VERSION}
    git push origin v${ARLAS_VERSION}
    #@see scripts/build-github-changelog-generator.sh if you need a fresher version of this tool
    docker run -it --rm -v "$(pwd)":/usr/local/src/your-app gisaia/github-changelog-generator:latest github_changelog_generator \
        -u gisaia -p ARLAS-server --token ${GITHUB_CHANGELOG_TOKEN} \
        --no-pr-wo-labels --no-issues-wo-labels --no-unreleased --issue-line-labels API,OGC,conf,security,documentation \
        --exclude-labels type:duplicate,type:question,type:wontfix,type:invalid \
        --bug-labels type:bug \
        --enhancement-labels  type:enhancement \
        --breaking-labels type:breaking \
        --enhancement-label "**New stuff:**" --issues-label "**Miscellaneous:**" --since-tag v2.5.3
    git tag -d v${ARLAS_VERSION}
    git push origin :v${ARLAS_VERSION}
    echo "=> Commit release version"
    git add openapi/openapi.json
    git add openapi/openapi.yaml
    git add docs/api
    git commit -a -m "release version ${ARLAS_VERSION}"
    git tag v${ARLAS_VERSION}
    git push origin v${ARLAS_VERSION}
    git push origin develop

    echo "=> Merge develop into master"
    git checkout master
    git pull origin master
    git merge origin/develop
    git push origin master

    echo "=> Rebase develop"
    git checkout develop
    git pull origin develop
    git rebase origin/master
else echo "=> Skip git push master"; fi

echo "=> Update project version for develop"
mvn versions:set -DnewVersion=${ARLAS_DEV_VERSION}-SNAPSHOT

echo "=> Update REST API version in JAVA source code"
sed -i.bak 's/\"'${FULL_API_VERSION}'\"/\"API_VERSION\"/' arlas-rest/src/main/java/io/arlas/server/rest/explore/ExploreRESTServices.java

if [ "$SIMULATE" == "NO" ]; then
    sed -i.bak 's/\"'${FULL_API_VERSION}'\"/\"'${API_DEV_VERSION}-SNAPSHOT'\"/' openapi/openapi.yaml
    sed -i.bak 's/\"'${FULL_API_VERSION}'\"/\"'${API_DEV_VERSION}-SNAPSHOT'\"/' openapi/openapi.json
    git add openapi/openapi.json
    git add openapi/openapi.yaml
    git commit -a -m "development version ${ARLAS_DEV_VERSION}-SNAPSHOT"
    git push origin develop
else echo "=> Skip git push develop"; fi
