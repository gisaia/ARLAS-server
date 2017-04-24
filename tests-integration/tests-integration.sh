#!/bin/bash

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}/..

# CLEAN
echo "===> kill/rm old containers if needed"
function clean {
	docker kill arlas-server
	docker rm arlas-server
	docker kill elasticsearch
	docker rm elasticsearch
}
clean

# PACKAGE
echo "===> package arlas-server"
mvn clean package
VERSION=`echo -e 'setns x=http://maven.apache.org/POM/4.0.0\ncat /x:project/x:version/text()' | xmllint --shell pom.xml | grep -v /`
echo "arlas-server:${VERSION}"
mv target/arlas-server-${VERSION}.jar target/arlas-server.jar

# BUILD
echo "===> build arlas-server"
docker build --tag=arlas-server:${VERSION} .
echo "===> pull elasticsearch"
docker pull docker.elastic.co/elasticsearch/elasticsearch:5.3.0

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
	docker.elastic.co/elasticsearch/elasticsearch:5.3.0
echo "===> wait for elasticsearch"
docker run --link elasticsearch:elasticsearch --rm busybox sh -c 'until nc -w 2 elasticsearch 9200; do sleep 1; done'

echo "===> start arlas-server"
docker run -ti -d \
	--name arlas-server \
	-p 19999:9999 \
	--link elasticsearch:elasticsearch \
	arlas-server:${VERSION}
echo "===> wait for arlas-server"
docker run --link arlas-server:arlas-server --rm busybox sh -c 'until nc -w 2 arlas-server 9999; do sleep 1; done'

# TEST
echo "===> run integration tests"
docker run -it --rm \
	-w /opt/maven \
	-v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	--link arlas-server:arlas-server \
	--link elasticsearch:elasticsearch \
	maven:3.5.0-jdk-8 \
	mvn verify
	
# CLEAN
clean