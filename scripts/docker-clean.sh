#!/bin/bash
set -e

function clean_exit {
    ARG=$?
    exit $ARG
}
trap clean_exit EXIT

echo "===> stop arlas-server stack"
export ELASTIC_DATADIR="/tmp"
docker compose -f docker-compose.yml -f docker-compose-elasticsearch.yml --project-name arlas down -v