SWAGGER_VERSION=''
API_LANGUAGE=''

usage="
*******************************************
$(basename "$0") [-v version] [-l language]
where:
    -v  the version of the swagger-codegen
    -l  the target language

An example: $(basename "$0") -v 2.2.3 -l python
*******************************************
"

if [ $# -lt 4 ]; then
  echo "Error executing the script, not enough arguments"
  echo "$usage"
  exit 1
fi

# Script is expecting two arguments, for example: -v 2.2.3 for version '2.2.3' and -l python for language 'python'
while getopts 'v:l:' flag; do
  case "${flag}" in
    v) SWAGGER_VERSION="${OPTARG}" ;;
    l) API_LANGUAGE="${OPTARG}" ;;
  esac
done

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

docker build --tag=gisaia/swagger-codegen-${API_LANGUAGE}:${SWAGGER_VERSION} -f Dockerfile-swagger-codegen-${API_LANGUAGE} .
docker push gisaia/swagger-codegen-${API_LANGUAGE}:${SWAGGER_VERSION}
rm -rf swagger-codegen