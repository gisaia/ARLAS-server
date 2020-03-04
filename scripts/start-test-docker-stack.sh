#!/bin/bash
set -e

function clean_exit {
    ARG=$?
    exit $ARG
}
trap clean_exit EXIT

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}/..

# CLEAR ENV VAR
unset ARLAS_PREFIX
unset ARLAS_APP_PATH
unset ARLAS_OGC_SERVER_URI
unset ARLAS_SERVICE_RASTER_TILES_ENABLE

# STOP PREVIOUS STACK
./scripts/docker-clean.sh

# START STACK
./scripts/docker-run.sh -es=/tmp --build
DOCKER_IP=$(docker-machine ip || echo "localhost")
echo "===> arlas-server running on http://${DOCKER_IP}:19999/arlas/"

# PACKAGE
echo "===> load test dataset"
docker run --rm \
    -w /opt/maven \
    -v $PWD:/opt/maven \
    -v $HOME/.m2:/root/.m2 \
    -e ARLAS_HOST="arlas-server" \
    -e ARLAS_PORT="9999" \
    -e ARLAS_ELASTIC_NODES="elasticsearch:9200" \
    -e ALIASED_COLLECTION=${ALIASED_COLLECTION:-false} \
    --net arlas_default \
    maven:3.5.0-jdk-8 \
    mvn exec:java -Dexec.mainClass="io.arlas.server.CollectionTool" -Dexec.classpathScope=test -Dexec.args="load" -pl arlas-tests -B

echo "===> Enjoy arlas-server API on http://${DOCKER_IP}:19999/arlas/swagger"