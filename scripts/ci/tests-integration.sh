#!/bin/bash
set -e

export ELASTIC_VERSION="8.13.3"

function clean_exit {
  ARG=$?
	echo "===> Exit status = ${ARG}"
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
export ELASTIC_VERSION=${ELASTIC_VERSION}

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}/../..

# TESTS SUITE
./scripts/ci/tests-integration-stage.sh --stage=REST
./scripts/ci/tests-integration-stage.sh --stage=REST_WKT_GEOMETRIES
./scripts/ci/tests-integration-stage.sh --stage=STAC
./scripts/ci/tests-integration-stage.sh --stage=WFS
./scripts/ci/tests-integration-stage.sh --stage=CSW
./scripts/ci/tests-integration-stage.sh --stage=DOC
./scripts/ci/tests-integration-stage.sh --stage=AUTH
