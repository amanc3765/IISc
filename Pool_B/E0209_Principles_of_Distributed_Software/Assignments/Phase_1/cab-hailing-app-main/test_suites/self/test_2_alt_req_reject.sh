#!/bin/bash

source ../header.sh 

# Scenario Description
# Multiple requests to same cab and all alternate requests should be rejected 

tst_wallet_reset
tst_ride_reset

# initialize variables
rideId=-1
nRides=-1
cabStatus=""

tst_cab_signIn 101 5
br


tst_ride_requestRide rideId 201 10 20
tst_ride_getCabStatus cabStatus 101
tst_cab_rideEnded 101 $rideId
br

tst_ride_requestRide rideId 201 25 30
tst_ride_getCabStatus cabStatus 101
tst_cab_rideEnded 101 $rideId
br

tst_ride_requestRide rideId 201 35 40
tst_ride_getCabStatus cabStatus 101
tst_cab_rideEnded 101 $rideId
br

tst_ride_requestRide rideId 201 45 50
tst_ride_getCabStatus cabStatus 101
tst_cab_rideEnded 101 $rideId
br

tst_cab_numRides nRides 101
br

if [ "$nRides" != "2" ];
then
    echo "Number of rides should be 2!"
    quit "no"
fi

quit "yes"