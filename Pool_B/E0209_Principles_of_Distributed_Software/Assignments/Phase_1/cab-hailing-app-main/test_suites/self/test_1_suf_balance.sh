#!/bin/bash

source ../header.sh 

# Scenario Description
# Customer doesn't have sufficient balance and shouldn't get a ride 
# when fare exceeds the wallet balance 

tst_wallet_reset
tst_ride_reset

# initialize variables
rideId=-1
nRides=-1
cabStatus=""

tst_cab_signIn 101 5

# now attempt a ride which has fare more than wallet balance
tst_ride_requestRide rideId 201 10 1000000

tst_ride_getCabStatus cabStatus 101

if [ "$cabStatus" != "available 5" ];
then
    echo "Ride shouldn't have started"
    quit "no"
fi

quit "yes"