#!/bin/bash

cd cab-service

echo "-----------------------CabService : Maven Build Start-----------------------"
./mvnw -DskipTests -q clean package
echo "-----------------------CabService : Maven Build End-----------------------"

docker build -q --tag pods/cab-service . 

cd ..

docker run -p 8080:8080 \
	--rm \
	--name cabservice-container \
	--network="host" \
	pods/cab-service &

