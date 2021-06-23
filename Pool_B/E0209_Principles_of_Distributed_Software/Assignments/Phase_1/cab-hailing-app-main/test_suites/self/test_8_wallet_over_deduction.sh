#!/bin/bash

source ../header.sh 

# Scenario Description
# Wallet : Money deduction beyond wallet capacity should fail

tst_wallet_reset
tst_ride_reset

# initialize variables
testPassed="yes"
rideId=-1
nRides=-1
cabStatus=""

balance=-1
tst_wallet_getBalance balance 201
br

# deduct all balance customer has
tst_wallet_deductAmount 201 $balance
br 

# attempt to deduct just 1 unit more
tst_wallet_deductAmount 201 1
br
if [ $? != 0 ];
then
    echo "Deduction shouldn't have happened"
    quit "no"
fi

# add money to wallet
tst_wallet_addAmount 201 1
br
# now deduction should be success
tst_wallet_deductAmount 201 1
if [ $? != 1 ];
then
    echo "Deduction should have happened"
    quit "no"
fi


quit "yes"

