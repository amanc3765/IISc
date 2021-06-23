
	clear && ./mvnw package && java -jar target/*.jar

	clear && docker build -t pods_demo_image .

	docker rm -f $(docker ps -a -q)
	clear && docker run --publish 8081:8080 pods_demo_image
	clear && docker run --publish 8082:8080 pods_demo_image

	localhost:8081/hello?name=myfile&content=This is my first docker demo.%0A
	localhost:8082/hello?name=myfile&content=Hope I am doing good :)%0A

	clear && docker exec -it jolly_morse sh
	clear && docker exec -it optimistic_goodall sh

	clear && docker start stupefied_carson
	clear && docker start bold_engelbart


--------------------------------------------------

minikube daemon
Therefore, any docker image available on your machine will not be
         visible inside minikube, and hence cannot be directly deployed.

1) minikube --driver=docker start

2) minikuble kubectl 
-- create deployment hello-minikube \
--image=k8s.gcr.io/echoserver:1.4

OR Upload to gcr 
OR push image to container
eval $(minikube docker-env
minikube kubectl \
-- create deployment hello-java \
--image=pods_demo_image 

3) minikube kubectl \
--expose deployment hello-java \
--type=LoadBalancer \
--port=8080

minikube tunnel