#!/bin/bash
set -e

function clean_maven {
    echo "===> clean maven repository"
	docker run --rm \
		-w /opt/maven \
		-v $PWD:/opt/maven \
		-v $HOME/.m2:/root/.m2 \
		maven:3.8.5-openjdk-17 \
		mvn clean -B
}

function clean_exit {
    ARG=$?
	echo "===> Exit status = ${ARG}"
    clean_maven
    exit $ARG
}
trap clean_exit EXIT

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
	mvn clean install -B

echo "===> prepare arlas-server artifact"
cp conf/configuration.yaml .
cp arlas-server/target/arlas-*.jar .
