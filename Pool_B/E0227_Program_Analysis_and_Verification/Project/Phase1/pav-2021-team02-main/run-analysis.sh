#!/usr/bin/env bash

set -e

## ./run-analysis-one.sh  <Dir>  <MainClass>  <TargetClass>  <TargetMethod>


#./run-analysis-one.sh "./target1-pub" "AddNumbers"  "AddNumbers"  "main"
#./run-analysis-one.sh "./target1-pub" "AddNumFun"   "AddNumFun"   "expr"


# XXX you can add / delete / comment / uncomment lines below
./run-analysis-one.sh "./target1-pub" "BasicTest1"   "BasicTest1"   "myIncrement"
./run-analysis-one.sh "./target1-pub" "BasicTest1"   "BasicTest1"   "mySum"
./run-analysis-one.sh "./target1-pub" "BasicTest1"   "BasicTest1"   "add_x"
./run-analysis-one.sh "./target1-pub" "BasicTest1"   "BasicTest1"   "myChoose"

./run-analysis-one.sh "./target2-mine" "MyTargetClass1" "MyTargetClass1" "test1Conditional"
./run-analysis-one.sh "./target2-mine" "MyTargetClass1" "MyTargetClass1" "test2Widening"
./run-analysis-one.sh "./target2-mine" "MyTargetClass1" "MyTargetClass1" "test3NestedLoops"
./run-analysis-one.sh "./target2-mine" "MyTargetClass1" "MyTargetClass1" "test4UnaryAssignment" 
./run-analysis-one.sh "./target2-mine" "MyTargetClass1" "MyTargetClass1" "test5BinaryExpressionsAdd" 
./run-analysis-one.sh "./target2-mine" "MyTargetClass1" "MyTargetClass1" "test6BinaryExpressionsSubtract" 
./run-analysis-one.sh "./target2-mine" "MyTargetClass1" "MyTargetClass1" "test7BinaryExpressionsMultiply" 
