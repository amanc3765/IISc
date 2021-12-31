#!/usr/bin/env bash

set -e

## ./run-analysis.sh  <Dir>  <MainClass>  <TargetClass>  <TargetMethod>



#./run-analysis.sh  "./target1-pub"  "BasicTest"   "BasicTest"   "myIncrement"
#./run-analysis.sh  "./target1-pub"  "BasicTest"   "BasicTest"   "mySum"
#./run-analysis.sh  "./target1-pub"  "BasicTest"   "BasicTest"   "add_x"

function build-pub-example() {
    SNO=$1
    TCLASS=$2
    TMETHOD=$3
    ./run-analysis.sh  "./target1-pub"  "BasicTest1"   "$TCLASS"   "$TMETHOD"
    ### remove exception edges from cfg.dot
    # sed -i '\_label="\\l+_d' cfg.dot

    dot -Tpdf -o cfg.pdf cfg.dot

    cp cfg.pdf "./target1-pub/tc$SNO-$TCLASS.$TMETHOD.pdf"
}


build-pub-example "01"  "BasicTest1"   "myIncrement"
build-pub-example "02"  "BasicTest1"   "mySum"
build-pub-example "03"  "BasicTest1"   "add_x"



########################################
# combine the *.eps into a single pdf

cd ./target1-pub/
#gs -sDEVICE=pdfwrite -dNOPAUSE -dPSFitPage -dEPSCrop -dBATCH -dSAFER -sOutputFile=pub-testcases.pdf *.eps
#gs -sDEVICE=pdfwrite -dNOPAUSE -dPSFitPage -dEPSCrop -dBATCH -dSAFER -sOutputFile=pub-testcases.pdf *.eps

# rm -f pubtc-cfg.pdf
# pdfunite tc*.pdf pubtc-cfg.pdf

## remove  the temp pdfs
# rm -f tc*.pdf

