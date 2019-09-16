#!/bin/bash

ELASTIC_RANGE=("7.0.1","7.1.0","7.2.1")

for i in "${ELASTIC_RANGE[@]}"
do
	./scripts/tests-integration.sh --es=$i
done