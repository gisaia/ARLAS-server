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

ES_VERSION="5.5.1"

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
cp target/arlas-server-${VERSION}.jar target/arlas-server.jar

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
	-p 29999:9999 \
	--link elasticsearch:elasticsearch \
	arlas-server:${VERSION}
echo "===> wait for arlas-server"
docker run --link arlas-server:arlas-server --rm busybox sh -c 'i=1; until nc -w 2 arlas-server 9999; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'

echo "=> Generate documentation"
mkdir tmp || echo "tmp exists"
curl -XGET http://localhost:29999/arlas/swagger.json -o tmp/swagger.json
curl -XGET http://localhost:29999/arlas/swagger.yaml -o tmp/swagger.yaml
mvn  swagger2markup:convertSwagger2markup post-integration-test
cp conf/configuration.yaml .
cp target/arlas-*.jar .
mv target/doc .
