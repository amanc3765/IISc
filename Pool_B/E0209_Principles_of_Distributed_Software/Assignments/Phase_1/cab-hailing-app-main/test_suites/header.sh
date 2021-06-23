#!/bin/bash

GREEN=$'\e[0;32m'
RED=$'\e[0;31m'
NC=$'\e[0m'

# BASEURL="http://localhost"
# CAB_SERVICE_PORT="8080"
# RIDE_SERVICE_PORT="8081"
# WALLET_SERVICE_PORT="8082"

# CAB_SERVICE_BASE="${BASEURL}:${CAB_SERVICE_PORT}"
# RIDE_SERVICE_BASE="${BASEURL}:${RIDE_SERVICE_PORT}"
# WALLET_SERVICE_BASE="${BASEURL}:${WALLET_SERVICE_PORT}"

CAB_SERVICE_BASE=$(minikube service cab-service --url)
RIDE_SERVICE_BASE=$(minikube service ride-service --url)
WALLET_SERVICE_BASE=$(minikube service wallet-service --url)

echo "CAB_SERVICE_BASE: $CAB_SERVICE_BASE"
echo "RIDE_SERVICE_BASE: $RIDE_SERVICE_BASE"
echo "WALLET_SERVICE_BASE: $WALLET_SERVICE_BASE"

function test_print(){
    echo "Hello World"
}

function tst_cab_signIn(){
    cabId=$1
    initPos=$2
    echo "tst_cab_signIn : CabId=${cabId}, initPos=${initPos}"

    response=$(curl -s "${CAB_SERVICE_BASE}/signIn?cabId=${cabId}&initialPos=${initPos}")

    echo "tst_cab_signIn : Response : ${response}"

    ret=0
    [[ $response = "true" ]] && ret=1 || ret=0
    return $ret
}

function tst_cab_signOut(){
    cabId=$1
    echo "tst_cab_signOut : CabId=${cabId}"

    response=$(curl -s "${CAB_SERVICE_BASE}/signOut?cabId=${cabId}")

    echo "tst_cab_signOut : Response : ${response}"

    ret=0
    [[ $response = "true" ]] && ret=1 || ret=0
    return $ret
}


function tst_cab_rideEnded(){
    cabId=$1
    rideId=$2
    echo "tst_cab_rideEnded : CabId=${cabId}, rideId=${rideId}"

    response=$(curl -s "${CAB_SERVICE_BASE}/rideEnded?cabId=${cabId}&rideId=${rideId}")

    echo "tst_cab_rideEnded : Response : ${response}"

    ret=0
    [[ $response = "true" ]] && ret=1 || ret=0
    return $ret
}


function tst_ride_requestRide(){
    #To write rideId in $1 (first param)
    custId=$2
    sourceLoc=$3
    destinationLoc=$4
    echo "tst_ride_requestRide : custId=${custId}, sourceLoc=${sourceLoc}, destinationLoc=${destinationLoc}"

    response=$(curl -s "${RIDE_SERVICE_BASE}/requestRide?custId=${custId}&sourceLoc=${sourceLoc}&destinationLoc=${destinationLoc}")

    echo "tst_ride_requestRide : Response: ${response}"
    eval "$1=\"$response\""
}

function tst_cab_numRides(){
    #To write rideId in $1 (first param)
    cabId=$2
   
    echo "tst_cab_numRides : CabId=${cabId}"

    response=$(curl -s "${CAB_SERVICE_BASE}/numRides?cabId=${cabId}")

    echo "tst_cab_numRides : Response numRides : ${response}"
    eval "$1=$response"
}

function tst_ride_getCabStatus(){
    #To write rideId in $1 (first param)
    cabId=$2
   
    echo "tst_cab_getCabStatus : CabId=${cabId}"

    response=$(curl -s "${RIDE_SERVICE_BASE}/getCabStatus?cabId=${cabId}")

    echo "tst_cab_getCabStatus : Response numRides : ${response}"
    eval "$1=\"$response\""
}

function tst_ride_reset(){
   
    echo "tst_ride_reset"

    response=$(curl -s "${RIDE_SERVICE_BASE}/reset")

    echo "tst_ride_reset : Response : ${response}"
}

function tst_wallet_reset(){
   
    echo "tst_wallet_reset"

    response=$(curl -s "${WALLET_SERVICE_BASE}/reset")

    echo "tst_wallet_reset : Response : ${response}"
}

function tst_wallet_getBalance(){
    #To write rideId in $1 (first param)
    custId=$2
   
    echo "tst_ride_getBalance : custId=${custId}"

    response=$(curl -s "${WALLET_SERVICE_BASE}/getBalance?custId=${custId}")

    echo "tst_ride_getBalance : Response getBalance : ${response}"
    eval "$1=$response"
}


function tst_wallet_deductAmount(){
    #To write rideId in $1 (first param)
    custId=$1
    amount=$2

    echo "tst_wallet_deductAmount : custId=${custId}, amount=${amount}"

    response=$(curl -s "${WALLET_SERVICE_BASE}/deductAmount?custId=${custId}&amount=${amount}")

    echo "tst_wallet_deductAmount : Response deductAmount : ${response}"
    
    ret=0
    [[ $response = "true" ]] && ret=1 || ret=0
    return $ret
}

function tst_wallet_addAmount(){
    #To write rideId in $1 (first param)
    custId=$1
    amount=$2

    echo "tst_wallet_addAmount : custId=${custId}, amount=${amount}"

    response=$(curl -s "${WALLET_SERVICE_BASE}/addAmount?custId=${custId}&amount=${amount}")

    echo "tst_wallet_addAmount : Response addAmount : ${response}"
    
    ret=0
    [[ $response = "true" ]] && ret=1 || ret=0
    return $ret
}


function br(){
    echo ""
}

function quit(){
    echo "-----------------------------------------"
    if [[ "$1" = *"no"* ]] ; then
        echo "${RED} Test Passing Status: NO ${NC}"
    else
        echo "${GREEN} Test Passing Status: YES ${NC}"
    fi

    exit
}

function tst_global_sleep_low(){
    echo "Waiting for parallel jobs to complete"
    # sleep 2
    wait
}

function tst_global_sleep_med(){
    echo "Waiting for parallel jobs to complete"
    # sleep 5
    wait
}

function tst_global_sleep_high(){
    echo "Waiting for parallel jobs to complete"
    # sleep 10
    wait
}