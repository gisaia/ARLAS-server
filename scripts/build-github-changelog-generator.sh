#!/bin/bash

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}/..

# BUILD AND PUSH IMAGE
docker build --tag=gisaia/github-changelog-generator:latest -f Dockerfile-gcg .
GCG_VERSION=`docker run -it --rm -v "$(pwd)":/usr/local/src/your-app gisaia/github-changelog-generator:latest github_changelog_generator --version | cut -d: -f2  | xargs`
echo $GCG_VERSION
docker tag gisaia/github-changelog-generator:latest gisaia/github-changelog-generator:$(echo "${GCG_VERSION}" | tr -d '\r')
docker push gisaia/github-changelog-generator:$(echo "${GCG_VERSION}" | tr -d '\r')
docker push gisaia/github-changelog-generator:latest