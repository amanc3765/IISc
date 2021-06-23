#! /bin/bash
source ../../header.sh 

rm shout*

# Scenario Description
# Check if the current total balance in all wallets is equal to 
# original total balance in all wallets (which is a constant) MINUS totalFare.

# every test case should begin with these two steps
tst_wallet_reset
br
tst_ride_reset
br

#Step 1 : cab 101 signs in
tst_cab_signIn 101 0
tst_cab_signIn 102 0
tst_cab_signIn 103 0
tst_cab_signIn 104 0
br

# Run multiple test scripts in parallel
./script11.sh shout1 201 &
./script11.sh shout2 202 &
./script11.sh shout3 203 &
./script11.sh shout4 201 &

tst_global_sleep_med

totalFare=0
for i in $(cat shout*);
do
  totalFare=$(expr $totalFare + $i)
done
echo "Total Fare: $totalFare"
br

#Total wallet balance after deductions
totalWalletBalance=0
for custID in 201 202 203
do
  balance=-1
  tst_wallet_getBalance balance $custID
  echo "Balance for Customer $custID : $balance"
  totalWalletBalance=$(expr $totalWalletBalance + $balance)
  br
done
echo "Total Wallet Balance: $totalWalletBalance"

initBalance=30000
actualBalance=$(expr $totalFare + $totalWalletBalance)

echo "$initBalance"
echo "$actualBalance"

# Deductions+New Balance should equal initial balance
if [ "$actualBalance" != "$initBalance" ];
then
    echo "Inconsistent wallet balance."
    quit "no"
fi

quit "yes"



