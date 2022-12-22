#!/bin/bash
set -e

function clean_exit {
    ARG=$?
	echo "===> Exit stage ${STAGE} = ${ARG}"
    exit $ARG
}
trap clean_exit EXIT

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}

docker run --rm \
    -v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	busybox \
        sh -c '(mkdir /opt/maven/target || echo "target exists") \
        && (mkdir /opt/maven/target/tmp || echo "target/tmp exists") \
        && (mkdir /opt/maven/target/tmp/typescript-fetch || echo "target/tmp/typescript-fetch exists") \
        && (mkdir /opt/maven/target/tmp/python-api || echo "target/tmp/python-api exists") \
        && (mkdir /opt/maven/target/generated-docs || echo "target/generated-docs exists") \
        && (mkdir /opt/maven/target/generated-docs/typescript-doc || echo "target/generated-docs/typescript-doc exists") \
        && (mkdir /opt/maven/target/generated-docs/python-doc || echo "target/generated-docs/python-doc exists") \
        && (cp -r /opt/maven/docs/* /opt/maven/target/generated-docs)'


echo "=> Generate API"
docker run --rm \
    --mount dst=/input/api.json,src="$PWD/openapi/swagger.json",type=bind,ro \
    --mount dst=/output,src="$PWD/target/tmp/typescript-fetch",type=bind \
	gisaia/swagger-codegen-2.4.14 \
        -l typescript-fetch --additional-properties modelPropertyNaming=snake_case

echo "=> Generate Typescript client documentation"
docker run --rm \
    -v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	busybox \
        sh -c '(cp /opt/maven/conf/npm/package-doc.json /opt/maven/target/tmp/typescript-fetch/package.json) \
        && (cp /opt/maven/conf/npm/tsconfig-build.json /opt/maven/target/tmp/typescript-fetch/tsconfig.json)'


echo "=> Generate Python API and its documentation"
docker run --rm \
    --mount dst=/input/api.json,src="$PWD/openapi/swagger.json",type=bind,ro \
    --mount dst=/input/config.json,src="$PWD/conf/swagger/python-config.json",type=bind,ro \
    --mount dst=/output,src="$PWD/target/tmp/python-api",type=bind \
	gisaia/swagger-codegen-2.4.14 \
        -l python --type-mappings GeoJsonObject=object

BASEDIR=$PWD

cd ${BASEDIR}/target/tmp/typescript-fetch/
docker run -a STDERR --rm  -i -v `pwd`:/docs gisaia/typedocgen:0.0.7 generatedoc api.ts
cd ${BASEDIR}


mkdir -p target/generated-docs/typescript-doc
docker run --rm \
    -v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	busybox \
        sh -c 'mv /opt/maven/target/tmp/typescript-fetch/typedoc_docs/* /opt/maven/target/generated-docs/typescript-doc'

mkdir -p target/generated-docs/python-doc

docker run --rm \
    -v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	busybox \
        sh -c 'mv /opt/maven/target/tmp/python-api/docs/* /opt/maven/target/generated-docs/python-doc'

echo "=> Generate API documentation"
docker run --rm \
    -v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	busybox \
        sh -c 'cat /opt/maven/docs/api/overview.md > /opt/maven/target/generated-docs/reference.md \
        && cat /opt/maven/docs/api/paths.md >> /opt/maven/target/generated-docs/reference.md \
        && cat /opt/maven/docs/api/definitions.md >> /opt/maven/target/generated-docs/reference.md \
        && cat /opt/maven/docs/api/security.md >> /opt/maven/target/generated-docs/reference.md'
        
echo "=> Copy CHANGELOG.md"
docker run --rm \
    -v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	busybox \
        sh -c 'cp /opt/maven/CHANGELOG.md /opt/maven/target/generated-docs/CHANGELOG_ARLAS-server.md'

echo "=> Check generated documentation"
if [[ ! -f ${BASEDIR}/target/generated-docs/typescript-doc/classes/ExploreApi.md ]] ; then
    echo 'File "ExploreApi.md" is not generated, aborting.'
    exit -1
fi
if [[ ! -f ${BASEDIR}/target/generated-docs/reference.md ]] ; then
    echo 'File "reference.md" is not generated, aborting.'
    exit -1
fi
