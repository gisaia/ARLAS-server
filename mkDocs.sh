#!/bin/bash
set -e

function clean_docker {
    ./scripts/docker-clean.sh
}

function clean_exit {
    ARG=$?
	echo "===> Exit stage ${STAGE} = ${ARG}"
    clean_docker
    exit $ARG
}
trap clean_exit EXIT

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}

# START ARLAS STACK
./scripts/docker-clean.sh
./scripts/docker-run.sh --build

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
    
echo "=> Generate API"
docker run --rm \
	-v $PWD:/opt/gen \
	-v $HOME/.m2:/root/.m2 \
	gisaia/swagger-codegen:2.3.1

echo "=> Generate Typescript client documentation"
docker run --rm \
    -v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	busybox \
        sh -c '(cp /opt/maven/conf/npm/package-build.json /opt/maven/target/tmp/typescript-fetch/package.json) \
        && (cp /opt/maven/conf/npm/tsconfig-build.json /opt/maven/target/tmp/typescript-fetch/tsconfig.json)'

echo "=> Generate Python API and its documentation"
docker run --rm \
	-v $PWD:/opt/gen \
	-v $HOME/.m2:/root/.m2 \
	gisaia/swagger-codegen-python:2.2.3

BASEDIR=$PWD

cd ${BASEDIR}/target/tmp/typescript-fetch/
docker run -a STDERR --rm  -i -v `pwd`:/docs gisaia/typedocgen:0.0.4 generatedoc api.ts
cd ${BASEDIR}

docker run --rm \
    -v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	busybox \
        sh -c '(mv /opt/maven/target/tmp/typescript-fetch/typedoc_docs/ /opt/maven/target/generated-docs \
        && mv /opt/maven/target/generated-docs/typedoc_docs/ /opt/maven/target/generated-docs/typescript-doc)'

docker run --rm \
    -v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	busybox \
        sh -c '(mv /opt/maven/target/tmp/python-api/docs/ /opt/maven/target/generated-docs \
        && mv /opt/maven/target/generated-docs/docs/ /opt/maven/target/generated-docs/python-doc \
        && mv /opt/maven/target/tmp/python-api/README.md /opt/maven/target/generated-docs/python-doc)'

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
        
echo "=> Copy CHANGELOG.md"
docker run --rm \
    -v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	busybox \
        sh -c 'cp /opt/maven/CHANGELOG.md /opt/maven/target/generated-docs/CHANGELOG_ARLAS-server.md'

echo "=> Check generated documentation"
if [[ ! -f ${BASEDIR}/target/generated-docs/typescript-doc/classes/_api_.exploreapi.md ]] ; then
    echo 'File "_api_.exploreapi.md" is not generated, aborting.'
    exit -1
fi
if [[ ! -f ${BASEDIR}/target/generated-docs/reference.md ]] ; then
    echo 'File "reference.md" is not generated, aborting.'
    exit -1
fi
