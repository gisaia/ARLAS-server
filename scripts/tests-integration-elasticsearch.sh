#!/bin/bash

ELASTIC_RANGE=("7.0.1","7.1.0","7.2.1","7.3.2","7.4.2","7.5.2","7.6.2","7.7.1","7.8.1","7.9.2")

for i in "${ELASTIC_RANGE[@]}"
do
	./scripts/tests-integration.sh --es=$i
done