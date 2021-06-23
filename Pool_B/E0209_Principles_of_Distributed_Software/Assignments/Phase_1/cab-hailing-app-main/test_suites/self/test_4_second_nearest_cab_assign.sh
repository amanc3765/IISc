#!/bin/bash

source ../header.sh 

# Scenario Description
# What if the nearest cab doesn't accept the requet. Second nearest cab (which is available and accepting)
# should be assigned.

tst_wallet_reset
tst_ride_reset

# initialize variables
rideId=-1
nRides=-1
cabStatus=""

# 2 cabs sign in 
tst_cab_signIn 101 10
tst_cab_signIn 102 50
br

# cab 101 should accept this
tst_ride_requestRide rideId 201 5 100
tst_ride_getCabStatus cabStatus 101
if [ "$cabStatus" != "giving-ride 5 201 100" ];
then
    echo "Cab assignment 1 failed!"
    quit "no"
fi
br
tst_cab_rideEnded 101 $rideId
br

# though cab 101 is at location 100 it shouldn't accept the following request
# cab 102 should accept the request
tst_ride_requestRide rideId 202 105 20
br
tst_ride_getCabStatus cabStatus 101
if [ "$cabStatus" != "available 100" ];
then
    echo "Cab assignment 2 failed : Cab 1 shouldn't be giving the ride"
    quit "no"
fi
tst_ride_getCabStatus cabStatus 102
if [ "$cabStatus" != "giving-ride 105 202 20" ];
then
    echo "Cab assignment 2 failed : Cab 2 should be giving the ride"
    quit "no"
fi

quit "yes"
