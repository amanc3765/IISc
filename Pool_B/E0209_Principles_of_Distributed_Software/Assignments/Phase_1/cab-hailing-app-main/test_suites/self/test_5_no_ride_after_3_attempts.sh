#!/bin/bash

source ../header.sh 

# Scenario Description
# There are multiple available cabs but if 3 nearest cabs reject
# then the ride request doesn't succeed even though other cabs are available

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

# cab 101 should accept this
tst_ride_requestRide rideId 201 6 10
tst_cab_rideEnded 101 $rideId
br

# cab 102 should accept this
tst_ride_requestRide rideId 201 16 20
tst_cab_rideEnded 102 $rideId
br

# cab 103 should accept this
tst_ride_requestRide rideId 201 26 30
tst_cab_rideEnded 103 $rideId
br

# cab 101,102,103 should reject this and 
# other cabs shouldn't be requested
tst_ride_requestRide rideId 201 5 10
br

if [ $rideId != -1 ];
then
    echo "Ride shouldn't have started!"
    quit "no"
fi

quit "yes"
