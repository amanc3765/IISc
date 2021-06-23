#!/bin/bash

cd db-service

docker build -q --tag pods/db-service . 

cd ..

docker run -p 9082:9082 \
	--rm \
	--name db-container \
	--network="host" \
	pods/db-service &