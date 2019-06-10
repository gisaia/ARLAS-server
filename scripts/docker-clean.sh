#!/bin/bash
set -e

function clean_exit {
    ARG=$?
    exit $ARG
}
trap clean_exit EXIT

echo "===> stop arlas-server stack"
docker-compose --project-name arlas down -v