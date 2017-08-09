#!/bin/bash
set -e

function clean_docker {
	docker kill arlas-server || echo "arlas-server already killed"
	docker rm arlas-server || echo "arlas-server already removed"
	docker kill elasticsearch || echo "elasticsearch already killed"
	docker rm elasticsearch || echo "elasticsearch already removed"

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

ES_VERSION="5.5.1"
for i in "$@"
do
case $i in
    --es=*)
    ES_VERSION="${i#*=}"
    shift # past argument=value
    ;;
    *)
            # unknown option
    ;;
esac
done

if [ -z ${ES_VERSION+x} ]; then usage; else echo "ARLAS-server tested with Elasticsearch version : ${ES_VERSION}"; fi

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}/..

# CLEAN
echo "===> kill/rm old containers if needed"
clean_docker

# PACKAGE
echo "===> package arlas-server"
mvn clean install
VERSION=`xmlstarlet sel -t -v /_:project/_:version pom.xml`
echo "arlas-server:${VERSION}"
mv target/arlas-server-${VERSION}.jar target/arlas-server.jar

# BUILD
echo "===> build arlas-server"
docker build --tag=arlas-server:${VERSION} .
echo "===> pull elasticsearch"
docker pull docker.elastic.co/elasticsearch/elasticsearch:${ES_VERSION}

# RUN
echo "===> start elasticsearch"
docker run -d \
	--name elasticsearch \
	-p 19200:9200 \
    -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
    -e xpack.security.enabled=false \
    -e xpack.monitoring.enabled=false \
    -e xpack.graph.enabled=false \
	-e xpack.watcher.enabled=false \
	docker.elastic.co/elasticsearch/elasticsearch:${ES_VERSION}
echo "===> wait for elasticsearch"
docker run --link elasticsearch:elasticsearch --rm busybox sh -c 'i=1; until nc -w 2 elasticsearch 9200; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'

echo "===> start arlas-server"
docker run -ti -d \
	--name arlas-server \
	-p 19999:9999 \
	--link elasticsearch:elasticsearch \
	arlas-server:${VERSION}
echo "===> wait for arlas-server"
docker run --link arlas-server:arlas-server --rm busybox sh -c 'i=1; until nc -w 2 arlas-server 9999; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'

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
	--link arlas-server:arlas-server \
	--link elasticsearch:elasticsearch \
	maven:3.5.0-jdk-8 \
	mvn install -DskipTests=false
