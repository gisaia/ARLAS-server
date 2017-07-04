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
	echo "Usage: ./release.sh -v=X.Y.Z -d=X.Y.Z+1 [--no-tests]"
	echo " -v|--version   release version"
	echo " -d|--dev       development version (-SNAPSHOT qualifier will be automatically added)"
	echo " --no-tests     do not run integration tests"
	exit 1
}

TESTS="YES"
for i in "$@"
do
case $i in
    -v=*|--version=*)
    VERSION="${i#*=}"
    shift # past argument=value
    ;;
    -d=*|--dev=*)
    DEV="${i#*=}"
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

if [ -z ${VERSION+x} ]; then usage; else echo "Release version          : ${VERSION}"; fi
if [ -z ${DEV+x} ]; then usage; else echo "Next development version : ${DEV}"; fi
echo "Running tests            : ${TESTS}"

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
	echo "=> Run integration tests"
	export ARLAS_HOST="localhost"; export ARLAS_PORT=9999; export ARLAS_PREFIX="/arlas/";
	export ARLAS_ELASTIC_HOST="localhost"; export ARLAS_ELASTIC_PORT=9300;
	mvn clean install -DskipTests=false
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
