#!/bin/bash

ELASTIC_RANGE=("5.0.2" "5.1.2" "5.2.2" "5.3.3" "5.4.3" "5.5.1")

for i in "${ELASTIC_RANGE[@]}"
do
	./tests-integration/tests-integration.sh --es=$i
done