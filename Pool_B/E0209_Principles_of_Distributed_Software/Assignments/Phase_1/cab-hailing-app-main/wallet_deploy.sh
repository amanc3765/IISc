#!/bin/bash

cd wallet-service

echo "-----------------------WalletService : Maven Build Start-----------------------"
./mvnw -DskipTests -q clean package
echo "-----------------------WalletService : Maven Build End-----------------------"

docker build -q --tag pods/wallet-service .

cd ..

docker run -p 8082:8082 \
	--rm \
	--name walletservice-container \
	--network="host" \
	pods/wallet-service &
	
# -v $(pwd)/init:/root/init \
