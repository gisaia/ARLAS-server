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
	echo " -es |--elastic-range     elasticsearch versions supported"
	echo " -api|--api-version       release arlas API version"
	echo " -rel|--arlas-release     release arlas-server version"
	echo " -dev|--arlas-dev         development arlas-server version (-SNAPSHOT qualifier will be automatically added)"
	echo " --no-tests               do not run integration tests"
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
    -api=*|--api-version=*)
    API_VERSION="${i#*=}"
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
if [ -z ${API_VERSION+x} ]; then usage;  else    echo "API version                    : ${API_VERSION}"; fi
if [ -z ${ARLAS_REL+x} ]; then usage;    else    echo "Release version                : ${ARLAS_REL}"; fi
if [ -z ${ARLAS_DEV+x} ]; then usage;    else    echo "Next development version       : ${ARLAS_DEV}"; fi
                                                 echo "Running tests                  : ${TESTS}"

VERSION="${API_VERSION}-${ELASTIC_RANGE}-${ARLAS_REL}"
DEV="${API_VERSION}-${ELASTIC_RANGE}-${ARLAS_DEV}"

echo "=> Get develop branch"
git checkout develop
git pull origin develop

echo "=> Update project version"
mvn versions:set -DnewVersion=${VERSION}

echo "=> Package arlas-server"
mvn clean install

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
	    ./tests-integration/tests-integration.sh --es=$i
    done
}
if [ "$TESTS" == "YES" ]; then itests; else echo "=> Skip integration tests"; fi

echo "=> Generate documentation"
curl -XGET http://localhost:9999/arlas/swagger.json -o doc/api/swagger/swagger.json
curl -XGET http://localhost:9999/arlas/swagger.yaml -o doc/api/swagger/swagger.yaml
markdown-pdf doc/api/API-definition.md -o doc/api/API-definition.pdf -r landscape -z doc/api/markdown2pdf.css
markdown-pdf doc/api/API-Collection-definition.md -o doc/api/API-Collection-definition.pdf -r landscape -z doc/api/markdown2pdf.css
swagger-codegen generate  -i doc/api/swagger/swagger.json  -l html2 -o doc/api/progapi/html/
swagger-codegen generate  -i doc/api/swagger/swagger.json  -l typescript-angular2 -o doc/api/progapi/typescript-angular2
swagger-codegen generate  -i doc/api/swagger/swagger.json  -l typescript-node -o doc/api/progapi/typescript-node
swagger-codegen generate  -i doc/api/swagger/swagger.json  -l typescript-fetch -o doc/api/progapi/typescript-fetch
mvn clean swagger2markup:convertSwagger2markup post-integration-test

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
git commit -a -m "development version ${DEV}-SNAPSHOT"
git push origin develop
