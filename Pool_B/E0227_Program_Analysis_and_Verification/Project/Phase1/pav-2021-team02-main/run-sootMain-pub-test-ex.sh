#!/usr/bin/env bash

set -e

######### alternatively
cd ./target1-pub/;
java -cp ../pkgs/soot-4.3.0-with-deps.jar  soot.Main -pp -cp . -f jimple -print-tags BasicTest1

ls sootOutput/
cat sootOutput/BasicTest1.jimple

