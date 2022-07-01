#!/bin/bash

DATE=`date`
MESSAGE="${DATE}: ${1}"

cd /home/aman/Desktop/MTech-Project
git add *
git commit -a -m "${MESSAGE}"
git push

echo "Pushed to git... ${MESSAGE}"