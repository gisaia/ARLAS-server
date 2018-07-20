#!/bin/bash

ELASTIC_RANGE=("6.0.1","6.1.3","6.2.4","6.3.1")

for i in "${ELASTIC_RANGE[@]}"
do
	./scripts/tests-integration.sh --es=$i
done