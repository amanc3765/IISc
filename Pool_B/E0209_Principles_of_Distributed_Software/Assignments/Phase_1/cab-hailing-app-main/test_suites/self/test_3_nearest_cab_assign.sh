#!/bin/bash

source ../header.sh 

# Scenario Description
# The nearest available accepting cab should always be assigned to the customer

tst_wallet_reset
tst_ride_reset

# initialize variables
rideId=-1
nRides=-1
cabStatus=""

# 5 cabs sign in 
tst_cab_signIn 101 10
tst_cab_signIn 102 20
tst_cab_signIn 103 30
tst_cab_signIn 104 40
tst_cab_signIn 105 50
br

# cab 105 should accept this
tst_ride_requestRide rideId 201 55 60
tst_ride_getCabStatus cabStatus 105
if [ "$cabStatus" != "giving-ride 55 201 60" ];
then
    echo "Cab assignment 1 failed!"
    quit "no"
fi
br

# cab 101 should accept this
tst_ride_requestRide rideId 202 5 10 
tst_ride_getCabStatus cabStatus 101
if [ "$cabStatus" != "giving-ride 5 202 10" ];
then
    echo "cab assignment 2 failed!"
    quit "no"
fi
br

# cab 103 should accept this
tst_ride_requestRide rideId 203 26 35 
tst_ride_getCabStatus cabStatus 103
if [ "$cabStatus" != "giving-ride 26 203 35" ];
then
    echo "cab assignment 3 failed!"
    quit "no"
fi
br

quit "yes"