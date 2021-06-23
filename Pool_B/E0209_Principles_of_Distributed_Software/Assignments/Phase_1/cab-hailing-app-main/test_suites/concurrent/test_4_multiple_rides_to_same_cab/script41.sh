#! /bin/bash
source ../../header.sh 

custId=$1

# Step 1: customer requests a cab until it gets one and this is done 4 times
rideDetails="-1"

for i in {1..3};
do 
    rideId=-1
    while true
    do
        tst_ride_requestRide rideDetails $custId 50 100
        rideId=$(echo $rideDetails | cut -d' ' -f 1)
        cabId=$(echo $rideDetails | cut -d' ' -f 2)
        if [ "$rideId" != "-1" ];
        then
            tst_cab_rideEnded $cabId $rideId
            break
        fi 
    done
done
