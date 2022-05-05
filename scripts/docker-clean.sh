#!/bin/bash
set -e

function clean_exit {
    ARG=$?
    exit $ARG
}
trap clean_exit EXIT

echo "===> stop arlas-server stack"
echo $(docker ps)
docker-compose -f docker-compose.yml -f docker-compose-elasticsearch.yml --project-name arlas down -v