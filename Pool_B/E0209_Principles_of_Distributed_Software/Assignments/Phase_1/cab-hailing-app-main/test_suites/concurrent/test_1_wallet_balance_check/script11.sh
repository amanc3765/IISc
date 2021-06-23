#! /bin/bash
source ../../header.sh 

outputFileName=$1
custId=$2

# Step 1: customer 201 requests a cab
rideDetails="-1"

tst_ride_requestRide rideDetails $custId 50 100
echo "$rideDetails"

rideId=$(echo $rideDetails | cut -d' ' -f 1)
cabId=$(echo $rideDetails | cut -d' ' -f 2)
fare=$(echo $rideDetails | cut -d' ' -f 3)

if [ "$rideId" == "-1" ];
then
    echo "Ride to customer 201 denied"
else
    echo "Ride by customer 201 started"
    echo $fare > $outputFileName
fi