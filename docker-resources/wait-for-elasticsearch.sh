#!/bin/sh -ex
set -e

script_name="$(basename "$(readlink -f "$0")")"

if [[ $# == 2 ]]; then
  host="$1"
  port="$2"
elif ! [[ -z ${ARLAS_ELASTIC_NODES+x} ]] && [[ $# == 0 ]]; then
  # No arguments are passed, & ARLAS_ELASTIC_NODES is set
  # Will use ARLAS_ELASTIC_NODES
  first_elasticsearch_node=${ARLAS_ELASTIC_NODES%%,*}
  host=${first_elasticsearch_node%:*}

  case "$first_elasticsearch_node" in
    *:*)
      # port specified, extracting it
      port=${first_elasticsearch_node#*:}    
      ;;
    *)
      # no port specified, using default
      port=9300
      ;;

  esac

else
  >&2 echo "[ERROR] $script_name -- host & port not correctly passed."
  exit 1
fi

i=1; until nc -w 2 $host $port; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); echo "try to connect to $host:$port"; done

java -jar /opt/app/arlas-server.jar server /opt/app/configuration.yaml