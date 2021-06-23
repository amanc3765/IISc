#! /bin/bash
source ../../header.sh 

for i in {0..10}
do
	echo "Shell 1:" $i
    tst_wallet_deductAmount 201 100
done