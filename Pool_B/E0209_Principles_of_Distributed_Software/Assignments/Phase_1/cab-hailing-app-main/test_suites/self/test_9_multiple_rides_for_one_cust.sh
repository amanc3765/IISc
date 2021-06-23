#!/bin/bash

source ../header.sh 

# Scenario Description
# What if the one customers requests ride while another ride is ongoing

tst_wallet_reset
tst_ride_reset

# initialize variables
rideId=-1
nRides=-1
cabStatus=""

# 5 cabs sign in 
tst_cab_signIn 101 10
tst_cab_signIn 102 50
br

tst_ride_requestRide rideId 201 50 100
if [ $? == -1 ];
then
    echo "First ride should've started"
    quit "no"
fi 

tst_ride_requestRide rideId 201 10 40
if [ $? == -1 ];
then
    echo "Second ride should've started"
    quit "no"
fi 

tst_ride_getCabStatus cabStatus 101
if [ "$cabStatus" != "giving-ride 10 201 40" ];
then
    echo "Invalid cab status for cab 101"
    quit "no"
fi
br

tst_ride_getCabStatus cabStatus 102
if [ "$cabStatus" != "giving-ride 50 201 100" ];
then
    echo "Invalid cab status for cab 102"
    quit "no"
fi
br

quit "yes"