#! /bin/bash
source ../../header.sh 

# Scenario Description
# Check if a cab is allowed to sign in multiple times

# every test case should begin with these two steps
tst_wallet_reset
br
tst_ride_reset
br


./script31.sh &
./script32.sh &
./script33.sh

tst_global_sleep_med

totalSignIn=0
for i in $(cat sh1out sh2out sh3out);
do
  totalSignIn=$(expr $totalSignIn + $i)
done
echo "Total Number of sign in requests accepted: $totalSignIn"
br

if [ "$totalSignIn" -ne "1" ];
then
    echo "Cab signed in multiple times."
    quit "no"
fi

quit "yes"

