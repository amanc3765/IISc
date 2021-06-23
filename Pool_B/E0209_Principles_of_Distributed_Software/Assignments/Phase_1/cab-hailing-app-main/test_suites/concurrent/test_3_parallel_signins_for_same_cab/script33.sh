#! /bin/bash
source ../../header.sh 
rm -f sh3out

#Step 1 : cab 101 signs in

tst_cab_signIn 101 0
isSignIn="$?"
echo "$isSignIn"
br
if [ $isSignIn == 1 ];
then
    echo "Request 3 for signin accepted."
else
    echo "Request 3 for signin rejected."
fi 
echo $isSignIn>>sh3out