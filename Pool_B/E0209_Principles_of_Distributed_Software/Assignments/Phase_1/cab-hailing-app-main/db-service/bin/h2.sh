#!/bin/bash

java \
   -cp h2.jar \
   org.h2.tools.Server \
   -web -webDaemon -webAllowOthers -webPort 9091 \
   -tcp -tcpAllowOthers -tcpPort 9092 \
   -baseDir ~ 