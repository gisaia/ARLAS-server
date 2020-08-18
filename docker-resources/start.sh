#!/bin/sh

fetchConfiguration(){
  echo "Download the ARLAS configuration file from "${ARLAS_CONFIGURATION_URL}" ..."
  curl ${ARLAS_CONFIGURATION_URL} -o /opt/app/configuration.yaml && echo "Configuration file downloaded with success." || (echo "Failed to download the configuration file. ARLAS will not start."; exit 1)
}

if [ -z "${ARLAS_CONFIGURATION_URL}" ]; then
  echo "The default ARLAS Server container configuration file is used"
else
  fetchConfiguration;
fi

if [ -z "${ARLAS_XMS}" ]; then
  ARLAS_XMS="512m";
  echo "Default value used for ARLAS_XMS:"$ARLAS_XMS
else
  echo "ARLAS_XMS"=$ARLAS_XMS
fi

if [ -z "${ARLAS_XMX}" ]; then
  ARLAS_XMX="512m";
  echo "Default value used for ARLAS_XMX:"$ARLAS_XMX
else
  echo "ARLAS_XMX"=$ARLAS_XMX
fi

java -Xms${ARLAS_XMS} -Xmx${ARLAS_XMX} -XX:+ExitOnOutOfMemoryError -jar arlas-server.jar server /opt/app/configuration.yaml
