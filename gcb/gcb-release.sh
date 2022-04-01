#!/bin/bash
set -o errexit -o pipefail

GITHUB_USERNAME=`git config user.name`
GITHUB_LOGIN=`git config user.email`
if  [ -z "$GITHUB_USERNAME"  ] ; then echo "Please set git config --global user.name"; exit -1; fi
if  [ -z "$GITHUB_LOGIN"  ] ; then echo "Please set git config --global user.email"; exit -1; fi
if  [ -z "$CLOUDSMITH_LOGIN"  ] ; then echo "Please set CLOUDSMITH_LOGIN environment variable"; exit -1; fi
if  [ -z "$CLOUDSMITH_API_KEY"  ] ; then echo "Please set CLOUDSMITH_API_KEY environment variable"; exit -1; fi

gcloud config configurations activate arlas-build

usage(){
	echo "Usage: ./gcb-release.sh -api=X -es=Y -rel=Z -dev=Z+1"
	echo " -api-major|--api-major-version       release arlas-server API major version"
	echo " -api-minor|--api-minor-version release arlas-server API minor version"
	echo " -api-patch|--api-patch-version release arlas-server API patch version"
	echo " -dev|--arlas-dev               development arlas-server version (-SNAPSHOT qualifier will be automatically added)"
	exit 1
}

for i in "$@"
do
case $i in
    -dev=*|--arlas-dev=*)
    ARLAS_DEV="${i#*=}"
    shift # past argument=value
    ;;
    -api-major=*|--api-major-version=*)
    API_MAJOR_VERSION="${i#*=}"
    shift # past argument=value
    ;;
    -api-minor=*|--api-minor-version=*)
    API_MINOR_VERSION="${i#*=}"
    shift # past argument=value
    ;;
    -api-patch=*|--api-patch-version=*)
    API_PATCH_VERSION="${i#*=}"
    shift # past argument=value
    ;;
    *)
            # unknown option
    ;;
esac
done


if [ -z ${API_MAJOR_VERSION+x} ]; then usage;  else    echo "API MAJOR version           : ${API_MAJOR_VERSION}"; fi
if [ -z ${API_MINOR_VERSION+x} ]; then usage;  else    echo "API MINOR version           : ${API_MINOR_VERSION}"; fi
if [ -z ${API_PATCH_VERSION+x} ]; then usage;  else    echo "API PATCH version           : ${API_PATCH_VERSION}"; fi
if [ -z ${ARLAS_DEV+x} ]; then usage;          else    echo "Next development version    : ${ARLAS_DEV}"; fi

export ARLAS_VERSION="${API_MAJOR_VERSION}.${API_MINOR_VERSION}.${API_PATCH_VERSION}"
ARLAS_DEV_VERSION="${API_MAJOR_VERSION}.${API_MINOR_VERSION}.${ARLAS_DEV}"
FULL_API_VERSION=${API_MAJOR_VERSION}"."${API_MINOR_VERSION}"."${API_PATCH_VERSION}
API_DEV_VERSION=${API_MAJOR_VERSION}"."${API_MINOR_VERSION}"."${ARLAS_DEV}

echo "Release : ${ARLAS_VERSION}"
echo "API     : ${FULL_API_VERSION}"
echo "Dev     : ${ARLAS_DEV_VERSION}"

gcloud builds submit \
 --substitutions=_ARLAS_VERSION=${ARLAS_VERSION},_ARLAS_DEV_VERSION=${ARLAS_DEV_VERSION},_FULL_API_VERSION=${FULL_API_VERSION},_API_DEV_VERSION=${API_DEV_VERSION},_CLOUDSMITH_LOGIN=${CLOUDSMITH_LOGIN},_CLOUDSMITH_API_KEY=${CLOUDSMITH_API_KEY},_GITHUB_LOGIN=${GITHUB_LOGIN},_GITHUB_USERNAME=${GITHUB_USERNAME},_GITHUB_CHANGELOG=${GITHUB_CHANGELOG_TOKEN}
