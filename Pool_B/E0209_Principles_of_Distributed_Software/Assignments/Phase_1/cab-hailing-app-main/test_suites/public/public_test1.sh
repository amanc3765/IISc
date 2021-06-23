#! /bin/sh
# this test case checks whether a customer's request
# gets rejected if only one cab has signed in but it is busy.

# reset RideService and Wallet.
# every test case should begin with these two steps
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

testPassed="yes"

#cab 101 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=0")
if [ "$resp" = "true" ];
then
	echo "Cab 101 signed in"
else
	echo "Cab 101 could not sign in"
	testPassed="no"
fi

#customer 201 requests a ride
rideId=$(curl -s \
"http://localhost:8081/requestRide?custId=201&sourceLoc=2&destinationLoc=10")
if [ "$rideId" != "-1" ];
then
	echo "Ride by customer 201 started"
else
	echo "Ride to customer 201 denied"
	testPassed="no"
fi

#customer 202 requests a ride
rideId=$(curl -s \
"http://localhost:8081/requestRide?custId=202&sourceLoc=1&destinationLoc=11")
if [ "$rideId" != "-1" ];
then
	echo "Ride by customer 202 started"
	testPassed="no"
else
	echo "Ride to customer 202 denied"
fi

echo "Test Passing Status" $testPassed

