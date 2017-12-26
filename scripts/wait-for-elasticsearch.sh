#!/bin/sh
set -e

host="$1"
port="$2"

i=1; until nc -w 2 $host $port; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); echo "try to connect to $host:$port"; done

java -jar /opt/app/arlas-server.jar server /opt/app/configuration.yaml