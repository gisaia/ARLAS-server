#!/bin/bash
set -e

function clean_docker {
    ./scripts/docker-clean.sh
    echo "===> clean maven repository"
	docker run --rm \
		-w /opt/maven \
		-v $PWD:/opt/maven \
		-v $HOME/.m2:/root/.m2 \
		maven:3.5.0-jdk-8 \
		mvn clean
}

function clean_exit {
    ARG=$?
  # Allow errors on cleanup
    set +e

    if [[ "$ARG" != 0 ]]; then
        # In case of error, print containers logs (if any)
        docker logs elasticsearch
        docker logs arlas-server
    fi
	echo "===> Exit stage ${STAGE} = ${ARG}"
    clean_docker
    exit $ARG
}
trap clean_exit EXIT

usage(){
	echo "Usage: ./test-integration-stage.sh --stage=REST|WFS"
	exit 1
}

for i in "$@"
do
case $i in
    --stage=*)
    STAGE="${i#*=}"
    shift # past argument=value
    ;;
    *)
            # unknown option
    ;;
esac
done

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}/..

# CHECK ALV2 DISCLAIMER
if [ $(find ./*/src -name "*.java" -exec grep -L Licensed {} \; | wc -l) -gt 0 ]; then
    echo "ALv2 disclaimer is missing in the following files :"
    find ./*/src -name "*.java" -exec grep -L Licensed {} \;
    exit -1
fi

if [ -z ${STAGE+x} ]; then usage; else echo "Tests stage : ${STAGE}"; fi

function start_stack() {
    # START ARLAS STACK
    ./scripts/docker-clean.sh
    OPTIONS=""
    ./scripts/docker-run.sh $OPTIONS -es=/tmp --build
}

# TEST
function test_rest() {
    export ARLAS_PREFIX="/arlastest"
    export ARLAS_APP_PATH="/pathtest"
    export ARLAS_SERVICE_TAG_ENABLE=true
    export ARLAS_SERVICE_WFS_ENABLE=true
    export ARLAS_INSPIRE_ENABLED=true
    export ARLAS_SERVICE_RASTER_TILES_ENABLE=true
    export ARLAS_BASE_URI="http://arlas-server:9999/pathtest/arlastest/"
    export ARLAS_TILE_URL="jar:file:///opt/app/arlas-server.jar!/{id}/{z}/{x}/{y}.png"
    start_stack
    docker run --rm \
        -w /opt/maven \
        -v $PWD:/opt/maven \
        -v $HOME/.m2:/root/.m2 \
        -e ARLAS_HOST="arlas-server" \
        -e ARLAS_PORT="9999" \
        -e ARLAS_PREFIX=${ARLAS_PREFIX} \
        -e ARLAS_APP_PATH=${ARLAS_APP_PATH} \
        -e ARLAS_SERVICE_TAG_ENABLE=${ARLAS_SERVICE_TAG_ENABLE} \
        -e ARLAS_INSPIRE_ENABLED=${ARLAS_INSPIRE_ENABLED=true}\
        -e ARLAS_SERVICE_RASTER_TILES_ENABLE=${ARLAS_SERVICE_RASTER_TILES_ENABLE} \
        -e ARLAS_TILE_URL=${ARLAS_TILE_URL} \
        -e ARLAS_ELASTIC_NODES="elasticsearch:9300" \
        -e ALIASED_COLLECTION=${ALIASED_COLLECTION} \
        -e WKT_GEOMETRIES=${WKT_GEOMETRIES} \
        --net arlas_default \
        maven:3.5.0-jdk-8 \
        mvn verify -DskipTests=false
}

function test_wfs() {
    sleep 30s
    export ARLAS_PREFIX="/arlastest"
    export ARLAS_APP_PATH="/pathtest"
    export ARLAS_BASE_URI="http://arlas-server:9999/pathtest/arlastest/"
    export ARLAS_SERVICE_WFS_ENABLE=true
    export ARLAS_INSPIRE_ENABLED=true
    start_stack
    docker run --rm \
        -w /opt/maven \
        -v $PWD:/opt/maven \
        -v $HOME/.m2:/root/.m2 \
        -e ARLAS_HOST="arlas-server" \
        -e ARLAS_PORT="9999" \
        -e ARLAS_PREFIX=${ARLAS_PREFIX} \
        -e ARLAS_APP_PATH=${ARLAS_APP_PATH} \
        -e ARLAS_INSPIRE_ENABLED=${ARLAS_INSPIRE_ENABLED} \
        -e ARLAS_ELASTIC_NODES="elasticsearch:9300" \
        -e ALIASED_COLLECTION=${ALIASED_COLLECTION} \
        --net arlas_default \
        maven:3.5.0-jdk-8 \
        mvn exec:java -Dexec.mainClass="io.arlas.server.CollectionTool" -Dexec.classpathScope=test -Dexec.args="load" -pl arlas-tests

    docker run --rm \
         --net arlas_default \
         --env ID="ID__170__20DI"\
         --env WFS_GETCAPABILITIES_URL="http://arlas-server:9999/pathtest/arlastest/ogc/wfs/geodata/?request=GetCapabilities&service=WFS&version=2.0.0" \
         gisaia/ets-wfs20:0.0.2

    docker run --rm \
        -w /opt/maven \
        -v $PWD:/opt/maven \
        -v $HOME/.m2:/root/.m2 \
        -e ARLAS_HOST="arlas-server" \
        -e ARLAS_PORT="9999" \
        -e ARLAS_PREFIX=${ARLAS_PREFIX} \
        -e ARLAS_APP_PATH=${ARLAS_APP_PATH} \
        -e ARLAS_ELASTIC_NODES="elasticsearch:9300" \
        -e ALIASED_COLLECTION=${ALIASED_COLLECTION} \
        --net arlas_default \
        maven:3.5.0-jdk-8 \
        mvn exec:java -Dexec.mainClass="io.arlas.server.CollectionTool" -Dexec.classpathScope=test -Dexec.args="delete" -pl arlas-tests
}


function test_csw() {
    export ARLAS_PREFIX="/arlastest"
    export ARLAS_APP_PATH="/pathtest"
    export ARLAS_BASE_URI="http://arlas-server:9999/pathtest/arlastest/"
    export ARLAS_SERVICE_CSW_ENABLE=true
    start_stack
    docker run --rm \
        -w /opt/maven \
        -v $PWD:/opt/maven \
        -v $HOME/.m2:/root/.m2 \
        -e ARLAS_HOST="arlas-server" \
        -e ARLAS_PORT="9999" \
        -e ARLAS_PREFIX=${ARLAS_PREFIX} \
        -e ARLAS_APP_PATH=${ARLAS_APP_PATH} \
        -e ARLAS_ELASTIC_NODES="elasticsearch:9300" \
        -e ALIASED_COLLECTION=${ALIASED_COLLECTION} \
        --net arlas_default \
        maven:3.5.0-jdk-8 \
        mvn exec:java -Dexec.mainClass="io.arlas.server.CollectionTool" -Dexec.classpathScope=test -Dexec.args="loadcsw" -pl arlas-tests

    docker run --rm \
         --net arlas_default \
         --env CSW_GETCAPABILITIES_URL="http://arlas-server:9999/pathtest/arlastest/ogc/csw/?" \
         gisaia/ets-cat30

    docker run --rm \
        -w /opt/maven \
        -v $PWD:/opt/maven \
        -v $HOME/.m2:/root/.m2 \
        -e ARLAS_HOST="arlas-server" \
        -e ARLAS_PORT="9999" \
        -e ARLAS_PREFIX=${ARLAS_PREFIX} \
        -e ARLAS_APP_PATH=${ARLAS_APP_PATH} \
        -e ARLAS_ELASTIC_NODES="elasticsearch:9300" \
        -e ALIASED_COLLECTION=${ALIASED_COLLECTION} \
        --net arlas_default \
        maven:3.5.0-jdk-8 \
        mvn exec:java -Dexec.mainClass="io.arlas.server.CollectionTool" -Dexec.classpathScope=test -Dexec.args="deletecsw" -pl arlas-tests
}

function test_doc() {
    ./mkDocs.sh
}

echo "===> run integration tests"
export ALIASED_COLLECTION="false"
if [ "$STAGE" == "REST" ]; then export ALIASED_COLLECTION="false"; export WKT_GEOMETRIES="false"; test_rest; fi
if [ "$STAGE" == "WFS" ]; then export ALIASED_COLLECTION="false"; export WKT_GEOMETRIES="false"; test_wfs; fi
if [ "$STAGE" == "CSW" ]; then export ALIASED_COLLECTION="false"; export WKT_GEOMETRIES="false"; test_csw; fi
if [ "$STAGE" == "REST_WKT_GEOMETRIES" ]; then export ALIASED_COLLECTION="false"; export WKT_GEOMETRIES="true"; test_rest; fi
if [ "$STAGE" == "REST_ALIASED" ]; then export ALIASED_COLLECTION="true"; export WKT_GEOMETRIES="false"; test_rest; fi
if [ "$STAGE" == "WFS_ALIASED" ]; then export ALIASED_COLLECTION="true"; export WKT_GEOMETRIES="false"; test_wfs; fi
if [ "$STAGE" == "CSW_ALIASED" ]; then export ALIASED_COLLECTION="true"; export WKT_GEOMETRIES="false"; test_csw; fi
if [ "$STAGE" == "DOC" ]; then test_doc; fi
