FROM adoptopenjdk:11-jre-hotspot

ARG JAR_FILE=target/*.jar
RUN mkdir -p /root/app 
RUN mkdir -p /root/init

COPY ${JAR_FILE} /root/app/cab_service.jar

WORKDIR /root/app
ENTRYPOINT ["java","-jar","cab_service.jar"]
