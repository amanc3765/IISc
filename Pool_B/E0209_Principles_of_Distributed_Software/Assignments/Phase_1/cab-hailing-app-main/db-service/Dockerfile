FROM adoptopenjdk:11-jre-hotspot

RUN mkdir -p /root/app 

# Port to expose for web console 
EXPOSE 9081 
# Port to expose for tcp connections
EXPOSE 9082

COPY bin/h2-*.jar /root/app/h2.jar
COPY bin/h2.sh    /root/app/

WORKDIR /root/app
RUN chmod +x h2.sh

#create an empty file for the db
RUN touch /root/ride-db.mv.db 

ENTRYPOINT ["bash","h2.sh"]
