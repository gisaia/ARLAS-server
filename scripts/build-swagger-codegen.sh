#!/bin/bash

SWAGGER_VERSION="2.3.1"

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}/..

# BUILD SWAGGER-CODEGEN SOURCES
git clone --branch v${SWAGGER_VERSION} https://github.com/swagger-api/swagger-codegen
cd swagger-codegen
docker run --rm \
    -w /opt/maven \
	-v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	maven:3.5-jdk-8-alpine \
	mvn package

# BUILD AND PUSH SWAGGER-CODEGEN IMAGE
cd ..
docker build --tag=gisaia/swagger-codegen:${SWAGGER_VERSION} -f Dockerfile-swagger-codegen .
docker push gisaia/swagger-codegen:${SWAGGER_VERSION}
rm -rf swagger-codegen