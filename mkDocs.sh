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

# Clean and Copy documentation to target/generated-docs
rm -rf target/generated-docs
mkdir -p target/generated-docs

echo "=> Get md documentation"
cp -r docs/docs/* target/generated-docs

# Get Typescript documentation
mkdir -p target/tmp/typescript-fetch

echo "=> Generate API"
docker run --rm \
    --mount dst=/input/api.json,src="$PWD/openapi/openapi.json",type=bind,ro \
    --mount dst=/output,src="$PWD/target/tmp/typescript-fetch",type=bind \
	gisaia/swagger-codegen-3.0.42 \
        -l typescript-fetch --additional-properties modelPropertyNaming=snake_case

echo "=> Generate Typescript client documentation"
docker run --rm \
    -v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	busybox \
        sh -c '(cp /opt/maven/conf/npm/package-doc.json /opt/maven/target/tmp/typescript-fetch/package.json) \
        && (cp /opt/maven/conf/npm/tsconfig-build.json /opt/maven/target/tmp/typescript-fetch/tsconfig.json)'

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

echo "=> Generate API documentation"
docker run --rm \
    -v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	busybox \
        sh -c 'cat /opt/maven/docs/api/reference.md > /opt/maven/target/generated-docs/reference.md'

echo "=> Copy documentation under docs/ repository"
docker run --rm \
    -v $PWD:/opt/maven \
	  -v $HOME/.m2:/root/.m2 \
	  busybox \
        sh -c 'mkdir -p /opt/maven/target/generated-docs \
        && cp -r /opt/maven/docs/* /opt/maven/target/generated-docs'

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
