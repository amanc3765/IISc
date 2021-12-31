#!/usr/bin/env bash

set -e

if [ $# -lt 4 ]; then
    echo "Usage: run-analysis.sh  dirname mainclass tclass tmethod"
    exit 1
fi

DIRNAME=$1
MAINCLASS=$2
TARGETCLASS=$3
TARGETMETHOD=$4

# export CLASSPATH=.:pkgs/soot-4.3.0-20210915.120431-213-jar-with-dependencies.jar
export CLASSPATH=.:pkgs/soot-4.3.0-with-deps.jar

echo === running Analysis.java
echo === Interval ANALYSIS

# time java  -Xms800m -Xmx3g Analysis "./target" "AddNumFun" "AddNumFun" "expr"



echo  "=== Running" Analysis "$DIRNAME" "$MAINCLASS"  "$TARGETCLASS"  "$TARGETMETHOD"

time \
    java -Xms800m -Xmx3g Analysis "$DIRNAME"  "$MAINCLASS"  "$TARGETCLASS"  "$TARGETMETHOD"


dot -Tpng -o cfg.png cfg.dot


