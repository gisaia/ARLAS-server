#!/bin/bash
set -e

export ARLAS_VERSION=`xmlstarlet sel -t -v /_:project/_:version pom.xml`
export ELASTIC_VERSION="5.6.5"

function clean_docker {
    echo "===> stop arlas-server stack"
    docker-compose --project-name arlas down
}

function clean_exit {
    ARG=$?
	echo "===> Exit status = ${ARG}"
    clean_docker
    exit $ARG
}
trap clean_exit EXIT

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}

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

echo "=> Start arlas-server stack"
docker-compose --project-name arlas up -d
DOCKER_IP=$(docker-machine ip || echo "localhost")

echo "=> Wait for arlas-server up and running"
i=1; until nc -w 2 ${DOCKER_IP} 19999; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done

echo "=> Get swagger documentation"
docker run --rm \
    -v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	busybox \
        sh -c '(mkdir /opt/maven/target || echo "target exists") \
        && (mkdir /opt/maven/target/tmp || echo "target/tmp exists") \
        && (mkdir /opt/maven/target/generated-docs || echo "target/generated-docs exists") \
        && (cp -r /opt/maven/docs/* /opt/maven/target/generated-docs)'
docker run --rm \
    --net arlas_default \
    -v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	--entrypoint sh \
	--network arlas_default \
	byrnedo/alpine-curl \
	-c 'i=1; until curl -XGET http://arlas-server:9999/arlas/swagger.json -o /opt/maven/target/tmp/swagger.json; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'
docker run --rm \
    -v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	--entrypoint sh \
	--network arlas_default \
	byrnedo/alpine-curl \
    -c 'i=1; until curl -XGET http://arlas-server:9999/arlas/swagger.yaml -o /opt/maven/target/tmp/swagger.yaml; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'

echo "=> Generate API documentation"
docker run --rm \
    -w /opt/maven \
	-v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	maven:3.5.0-jdk-8 \
    mvn swagger2markup:convertSwagger2markup post-integration-test
docker run --rm \
    -v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	busybox \
        sh -c 'cat /opt/maven/target/generated-docs/overview.md > /opt/maven/target/generated-docs/reference.md \
        && cat /opt/maven/target/generated-docs/paths.md >> /opt/maven/target/generated-docs/reference.md \
        && cat /opt/maven/target/generated-docs/definitions.md >> /opt/maven/target/generated-docs/reference.md \
        && cat /opt/maven/target/generated-docs/security.md >> /opt/maven/target/generated-docs/reference.md'
