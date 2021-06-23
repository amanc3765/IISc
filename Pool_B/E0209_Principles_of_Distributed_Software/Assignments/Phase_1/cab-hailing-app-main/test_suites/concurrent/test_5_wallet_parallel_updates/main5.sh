#! /bin/bash
source ../../header.sh 

# Scenario Description
# Many concurrent addition and deduction to a single user's wallet
# In the end we check whether the total initial balance equals the
# current balance as we have added and subtracted the same amount 
# effectively

# every test case should begin with these two steps
tst_wallet_reset
br
tst_ride_reset
br

startBalance=-1
tst_wallet_getBalance startBalance 201

./script51.sh &
./script52.sh 

tst_global_sleep_low

endBalance=-1
tst_wallet_getBalance endBalance 201

echo "startBalance : $startBalance"
echo "endBalance : $endBalance"

if [ $startBalance != $endBalance ];
then
    quit "no"
fi

quit "yes"
