#!/bin/bash
set -e

BUILD_OPTS="--no-build"

for i in "$@"
do
case $i in
    -es=*|--elasticsearch=*)
    export ELASTIC_DATADIR="${i#*=}"
    DOCKER_COMPOSE_ARGS="${DOCKER_COMPOSE_ARGS} -f docker-compose-elasticsearch.yml"
    shift # past argument=value
    ;;
    --build)
    BUILD_OPTS="--build"
    shift # past argument with no value
    ;;
    *)
            # unknown option
    ;;
esac
done

function clean_exit {
    ARG=$?
    exit $ARG
}
trap clean_exit EXIT

export ARLAS_VERSION=`xmlstarlet sel -t -v /_:project/_:version pom.xml`

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}/..

# PACKAGE
echo "===> compile arlas-server"
docker run --rm \
    -w /opt/maven \
	-v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	maven:3.8.4-openjdk-17 \
	mvn clean install -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
echo "arlas-server:${ARLAS_VERSION}"

echo "===> start arlas-server stack"
docker-compose -f docker-compose.yml ${DOCKER_COMPOSE_ARGS} --project-name arlas up -d ${BUILD_OPTS}

#docker logs -f arlas-server &

echo "===> wait for arlas-server up and running"
docker run --net arlas_default --rm busybox sh -c 'i=1; until nc -w 2 arlas-server 9999; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'
