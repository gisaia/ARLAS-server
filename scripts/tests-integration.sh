#!/bin/bash
set -e

export ARLAS_VERSION=`xmlstarlet sel -t -v /_:project/_:version pom.xml`
export ELASTIC_VERSION="5.6.5"

function clean_docker {
    echo "===> stop arlas-server stack"
    docker-compose --project-name arlas down
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
	echo "===> Exit status = ${ARG}"
    clean_docker
    exit $ARG
}
trap clean_exit EXIT

usage(){
	echo "Usage: ./test-integration.sh [--es=X.Y.Z]"
	echo " --es=X.Y.Z   elasticsearch version to test"
	exit 1
}

for i in "$@"
do
case $i in
    --es=*)
    ELASTIC_VERSION="${i#*=}"
    shift # past argument=value
    ;;
    *)
            # unknown option
    ;;
esac
done

if [ -z ${ELASTIC_VERSION+x} ]; then usage; else echo "ARLAS-server tested with Elasticsearch version : ${ELASTIC_VERSION}"; fi

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}/..

# CLEAN
echo "===> kill/rm old containers if needed"
clean_docker

# PACKAGE
echo "===> compile arlas-server"
docker run --rm \
    -w /opt/maven \
	-v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	maven:3.5.0-jdk-8 \
	mvn clean install
echo "arlas-server:${ARLAS_VERSION}"

# BUILD
echo "===> build arlas-server docker image"
docker build --tag=arlas-server:${ARLAS_VERSION} -f Dockerfile-package-only .

echo "===> start arlas-server stack"
docker-compose --project-name arlas up -d

echo "===> wait for arlas-server up and running"
docker run --net arlas_default --rm busybox sh -c 'i=1; until nc -w 2 arlas-server 9999; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'

# TEST
echo "===> run integration tests"
docker run --rm \
	-w /opt/maven \
	-v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	-e ARLAS_HOST="arlas-server" \
	-e ARLAS_PORT="9999" \
	-e ARLAS_PREFIX="/arlas/" \
	-e ARLAS_ELASTIC_HOST="elasticsearch" \
	-e ARLAS_ELASTIC_PORT="9300" \
	--net arlas_default \
	maven:3.5.0-jdk-8 \
	mvn install -DskipTests=false

echo "===> run integration WFS tests"
docker run --rm \
    -w /opt/maven \
    -v $PWD:/opt/maven \
    -v $HOME/.m2:/root/.m2 \
    -e ARLAS_HOST="arlas-server" \
    -e ARLAS_PORT="9999" \
    -e ARLAS_PREFIX="/arlas/" \
    -e ARLAS_ELASTIC_HOST="elasticsearch" \
    -e ARLAS_ELASTIC_PORT="9300" \
    --net arlas_default \
    maven:3.5.0-jdk-8 \
    mvn exec:java -Dexec.mainClass="io.arlas.server.CollectionTool" -Dexec.classpathScope=test -Dexec.args="load"

docker run --rm \
     --net arlas_default \
     --env ID="ID__170__20DI"\
     --env WFS_GETCAPABILITIES_URL="http://arlas-server:9999/arlas/wfs/geodata/?request=GetCapabilities&service=WFS&version=2.0.0" \
     gisaia/ets-wfs20

docker run --rm \
    -w /opt/maven \
    -v $PWD:/opt/maven \
    -v $HOME/.m2:/root/.m2 \
    -e ARLAS_HOST="arlas-server" \
    -e ARLAS_PORT="9999" \
    -e ARLAS_PREFIX="/arlas/" \
    -e ARLAS_ELASTIC_HOST="elasticsearch" \
    -e ARLAS_ELASTIC_PORT="9300" \
    --net arlas_default \
    maven:3.5.0-jdk-8 \
    mvn exec:java -Dexec.mainClass="io.arlas.server.CollectionTool" -Dexec.classpathScope=test -Dexec.args="delete"
