#!/bin/bash

source ../header.sh 

# Scenario Description
# Cab with ongoing ride shouldn't be able to signout

tst_wallet_reset
tst_ride_reset

# initialize variables
rideId=-1
nRides=-1
cabStatus=""

# 5 cabs sign in 
tst_cab_signIn 101 10
br

tst_ride_requestRide rideId 201 55 60
tst_ride_getCabStatus cabStatus 101
if [ "$cabStatus" != "giving-ride 55 201 60" ];
then
    echo "Cab assignment failed!"
    quit "no"
fi
br

tst_cab_signOut 101
if [ $? != 0 ];
then
    echo "Cab shouldn't be able to sign out!"
    quit "no"
fi
br

tst_cab_rideEnded 101 $rideId
br
tst_cab_signOut 101
if [ $? != 1 ];
then
    echo "Cab should be able to sign out!"
    quit "no"
fi
br

quit "yes"