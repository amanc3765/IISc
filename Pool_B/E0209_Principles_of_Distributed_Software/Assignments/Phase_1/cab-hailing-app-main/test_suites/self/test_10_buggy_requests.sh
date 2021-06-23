#!/bin/bash

source ../header.sh 

# Scenario Description
# A bunch of buggy (invalid) requests shouldn't succeed

tst_wallet_reset
tst_ride_reset

# initialize variables
rideId=-1
nRides=-1
cabStatus=""

# cabs sign in with invalid parameters
tst_cab_signIn 0 10
if [ $? != 0 ];
then
    echo "Cab 1 shouldn't be signed in"
    quit "no"
fi 
br
tst_cab_signIn 101 -50
if [ $? != 0 ];
then
    echo "Cab 2 shouldn't be signed in"
    quit "no"
fi 

#invalid wallet requests
tst_wallet_deductAmount 201 -20
if [ $? != 0 ];
then
    echo "Wallet operation 1 shouldn't be possible"
    quit "no"
fi 
br
tst_wallet_addAmount 201 -20
if [ $? != 0 ];
then
    echo "Wallet operation 2 shouldn't be possible"
    quit "no"
fi

quit "yes"