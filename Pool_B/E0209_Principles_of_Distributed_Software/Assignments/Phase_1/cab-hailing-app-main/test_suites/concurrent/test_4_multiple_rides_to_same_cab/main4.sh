#! /bin/bash
source ../../header.sh 

# Scenario Description
# Many parallel requests will come for two cabs. Each thread will try to get r rides 
# and continuously try for it. In the end the total number of rides should be equal to 
# r * no_of_threads

# every test case should begin with these two steps
tst_wallet_reset
br
tst_ride_reset
br

#Step 1: 2 cabs sign in
tst_cab_signIn 101 10
tst_cab_signIn 102 10


# Let's spawn 6 threads who will compete to get rides
./script41.sh 201 &
./script41.sh 201 &
./script41.sh 201 &
./script41.sh 202 &
./script41.sh 202 &
./script41.sh 202 &

tst_global_sleep_med

nRides1=-1
tst_cab_numRides nRides1 101
nRides2=-1
tst_cab_numRides nRides2 102

echo "Num Rides (Cab 1) : $nRides1"
echo "Num Rides (Cab 2) : $nRides2" 

totalRides=$(( nRides1 + nRides2 ))

if [ "$totalRides" -ne "18" ];   
then
    echo "Invalid number of rides given : $totalRides"
    quit "no"
fi

quit "yes"
