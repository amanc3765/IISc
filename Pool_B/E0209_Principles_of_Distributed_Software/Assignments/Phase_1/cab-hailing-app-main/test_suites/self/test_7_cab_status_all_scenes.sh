#!/bin/bash

source ../header.sh 

# Scenario Description
# Correct cab status string should be returned in all possible scenarios

tst_wallet_reset
tst_ride_reset

# initialize variables
rideId=-1
nRides=-1
cabStatus=""

# signed out status
tst_ride_getCabStatus cabStatus 101
if [ "$cabStatus" != "signed-out -1" ];
then
    echo "Status should be signed out!"
    quit "no"
fi
br

tst_cab_signIn 101 10
tst_ride_getCabStatus cabStatus 101
if [ "$cabStatus" != "available 10" ];
then
    echo "Status should be signed in!"
    quit "no"
fi
br

tst_ride_requestRide rideId 201 30 40
br
tst_ride_getCabStatus cabStatus 101
if [ "$cabStatus" != "giving-ride 30 201 40" ];
then
    echo "Status should be giving ride!"
    quit "no"
fi
br

tst_cab_rideEnded 101 $rideId
br
tst_ride_getCabStatus cabStatus 101
if [ "$cabStatus" != "available 40" ];
then
    echo "Status should be available!"
    quit "no"
fi
br

quit "yes"