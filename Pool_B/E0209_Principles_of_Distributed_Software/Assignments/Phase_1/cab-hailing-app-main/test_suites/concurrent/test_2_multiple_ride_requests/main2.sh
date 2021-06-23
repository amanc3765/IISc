source ../../header.sh 

rm shout*

# Scenario Description
# Multiple cabs will sign in. Multiple concurrent rideRequest will 
# be made and let's check if the app gives more rides than number of
# cabs. No ride will be ended.

# every test case should begin with these two steps
tst_wallet_reset
br
tst_ride_reset
br


#Step 1 : Many cabs sign in
tst_cab_signIn 101 0
tst_cab_signIn 102 0
tst_cab_signIn 103 0
tst_cab_signIn 104 0


for i in {1..12};
do
  outFile="shout$i"
  ./script21.sh $outFile 201 &
done

tst_global_sleep_med

totalRides=0
for i in $(cat shout*);
do
  totalRides=$(expr $totalRides + $i)
done
echo "Total Rides: $totalRides"
br

if [ "$totalRides" -ne "4" ];
then
    echo "Inconsistent number of rides."
    quit "no"
fi

quit "yes"



