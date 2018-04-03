#!/bin/bash

ELASTIC_RANGE=("6.0.1","6.1.3","6.2.3")

for i in "${ELASTIC_RANGE[@]}"
do
	./scripts/tests-integration.sh --es=$i
done