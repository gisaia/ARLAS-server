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
	maven:3.8.5-openjdk-17 \
	mvn clean install -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
echo "arlas-server:${ARLAS_VERSION}"

echo "===> start arlas-server stack"
if [ -z "${ELASTIC_DATADIR}" ]; then
  echo "An external ES is used"
else
  echo "Starting ES"
  docker-compose -f docker-compose-elasticsearch.yml --project-name arlas up -d
  echo "Waiting for ES readiness"
  docker run --net arlas_default --rm busybox sh -c 'i=1; until nc -w 2 elasticsearch 9200; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'
  echo "ES is ready"
fi

docker-compose -f docker-compose.yml --project-name arlas up -d ${BUILD_OPTS}

#docker logs -f arlas-server &

echo "===> wait for arlas-server up and running"
docker run --net arlas_default --rm busybox sh -c 'i=1; until nc -w 2 arlas-server 9999; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'
