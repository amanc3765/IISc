#! /bin/bash
source ../header.sh 


function loop() {
    script=$1

    for i in {1..3};
    do
        echo "Running test : ${PWD##*/} : Iteration $i"
        bash "$script"
    done	
    
}

var=""

br
cd test_1_*
echo "------------ Press any key to start Test 1 : ${PWD##*/} -------"
read var1
loop "main1.sh"

br
cd ../test_2_*
echo "------------ Press any key to start Test 2 : ${PWD##*/} -------"
read var1
loop "main2.sh"

br
cd ../test_3_*
echo "------------ Press any key to start Test 3 : ${PWD##*/} -------"
read var1
loop "main3.sh"

br
cd ../test_4_*
echo "------------ Press any key to start Test 4 : ${PWD##*/} -------"
read var1
loop "main4.sh"

br
cd ../test_5_*
echo "------------ Press any key to start Test 5 : ${PWD##*/} -------"
read var1
loop "main5.sh"