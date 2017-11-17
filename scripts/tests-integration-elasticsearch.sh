#!/bin/bash

ELASTIC_RANGE=("5.0.2" "5.1.2" "5.2.2" "5.3.3" "5.4.3" "5.5.3" "5.6.3")

for i in "${ELASTIC_RANGE[@]}"
do
	./scripts/tests-integration.sh --es=$i
done