#!/bin/bash

source ../header.sh 

# Scenario Description
# Test response form requestRide

# initialize variables
rideDetails="-1"

tst_wallet_reset
br
tst_ride_reset
br
tst_cab_signIn 101 0
br
tst_ride_requestRide rideDetails 201 0 100
echo "$rideDetails"

rideId=$(echo $rideDetails | cut -d' ' -f 1)
cabId=$(echo $rideDetails | cut -d' ' -f 2)
fare=$(echo $rideDetails | cut -d' ' -f 3)

if [ "$rideId" == "-1" ];
then
    echo "Ride could not be started."
    quit "no"
fi

quit "yes"