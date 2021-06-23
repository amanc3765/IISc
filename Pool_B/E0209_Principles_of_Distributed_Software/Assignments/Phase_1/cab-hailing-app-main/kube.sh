#!/bin/bash

GREEN=$'\e[0;32m'
RED=$'\e[0;31m'
NC=$'\e[0m'

# Reference for autoscaling in minikube : https://faun.pub/kubernetes-horizontal-pod-autoscaler-hpa-bb789b3070e4

# changes done in yaml : specify resource limits for container

# Enable metric server : minikube addons enable metrics-server

var1=""

echo "${RED}Ensure that minikube is running. Use the following command${NC}"
echo "${GREEN}minikube start --extra-config=controller-manager.horizontal-pod-autoscaler-upscale-delay=1m --extra-config=controller-manager.horizontal-pod-autoscaler-downscale-delay=1m --extra-config=controller-manager.horizontal-pod-autoscaler-sync-period=10s --extra-config=controller-manager.horizontal-pod-autoscaler-downscale-stabilization=1m${NC}"
read var1

echo "Running minikube docker-env"
eval $(minikube docker-env)

echo "______________________________________________________"

echo "Minikube Tunnel"
echo "${RED}Ensure that minikube tunnel is running and press enter.${NC}"
read var1


echo "______________________________________________________"

# -------------------------------------------------------------------------------------------------------------------

echo "${RED}Do you want to do Maven Build ? (y/n)${NC}"
read var1
if [ "$var1" == "y" ]; then
	./maven_build.sh
fi

echo "______________________________________________________"

# -------------------------------------------------------------------------------------------------------------------

echo "${RED}Would you like to build docker images? (y/n)${NC}"
read var1

if [ "$var1" == "y" ]; then
	docker build -q --tag pods/db-service db-service 
	docker build -q --tag pods/cab-service cab-service 
	docker build -q --tag pods/ride-service ride-service 
	docker build -q --tag pods/wallet-service wallet-service 
else
	echo "Skipping building images"
fi

echo "______________________________________________________"

# -------------------------------------------------------------------------------------------------------------------

echo "Deleting existing deployments for minikube"
echo "______________________________________________________"

minikube kubectl delete deployment db-service
minikube kubectl delete deployment cab-service
minikube kubectl delete deployment ride-service
minikube kubectl delete deployment wallet-service

echo "______________________________________________________"

# -------------------------------------------------------------------------------------------------------------------

echo "Creating deployments for minikube"
echo "______________________________________________________"


minikube kubectl -- apply -f db-service/db.yml 
minikube kubectl -- apply -f cab-service/cab.yml 
minikube kubectl -- apply -f wallet-service/wallet.yml 
echo "${RED}Please ensure that external db-service has started before running ride-service. Press enter when ready.${NC}"
read var1
minikube kubectl -- apply -f ride-service/ride.yml 


echo "______________________________________________________"

# -------------------------------------------------------------------------------------------------------------------

echo "Creating HPA (HorizontalPodAutoscalar) with 1-4 range of pods for Ride Service"
echo "______________________________________________________"
minikube kubectl -- delete hpa ride-service
minikube kubectl -- autoscale deployment ride-service --cpu-percent=25 --min=1 --max=4


# -------------------------------------------------------------------------------------------------------------------


echo "${RED}Would you like to delete existing services? (y/n)${NC}"
read var1
echo "______________________________________________________"

if [ "$var1" == "y" ]; then
	minikube kubectl delete service db-console-service
	minikube kubectl delete service db-tcp-service
	minikube kubectl delete service cab-service
	minikube kubectl delete service ride-service
	minikube kubectl delete service wallet-service
fi

echo "______________________________________________________"

# -------------------------------------------------------------------------------------------------------------------

echo "Exposing deployments for minikube"
echo "______________________________________________________"

minikube kubectl -- expose deployment db-service \
           		 --type=LoadBalancer --port=9091 --target-port=9091  \
           		 --name=db-console-service
                
minikube kubectl -- expose deployment db-service \
           		 --type=LoadBalancer --port=9092 --target-port=9092  \
           		 --name=db-tcp-service
                                
minikube kubectl -- expose deployment cab-service \
           		 --type=LoadBalancer --port=8080 --target-port=8080  
                
minikube kubectl -- expose deployment ride-service \
           		 --type=LoadBalancer --port=8080 --target-port=8081  
     
minikube kubectl -- expose deployment wallet-service \
          		 --type=LoadBalancer --port=8080 --target-port=8082    

echo "______________________________________________________"



# -------------------------------------------------------------------------------------------------------------------

echo "View deployments for minikube : minikube kubectl -- get deployments"
echo "______________________________________________________"

minikube kubectl -- get deployments

echo "______________________________________________________"

# -------------------------------------------------------------------------------------------------------------------

echo "View pods for minikube : minikube kubectl -- get pods"
echo "______________________________________________________"

minikube kubectl -- get pods

echo "______________________________________________________"

# -------------------------------------------------------------------------------------------------------------------

echo "View services for minikube : minikube kubectl -- get services"
echo "______________________________________________________"

minikube kubectl -- get services

echo "______________________________________________________"

echo "View HP autoscalar status for ride service : minikube kubectl -- get hpa"
echo "______________________________________________________"

minikube kubectl -- get hpa

echo "______________________________________________________"



# -------------------------------------------------------------------------------------------------------------------

# #To scale a deployment 
# minikube kubectl -- scale --replicas=0 deployments/cab-service deployments/ride-service deployments/wallet-service
# minikube kubectl -- scale --replicas=1 deployments/cab-service deployments/wallet-service
# minikube kubectl -- scale --replicas=3 deployments/ride-service

# View logs
# minikube kubectl -- get pods
# minikube kubectl -- logs ...

# docker container stop $(docker container ls -aq)
# docker container rm $(docker container ls -aq)

