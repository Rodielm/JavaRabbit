#!/bin/bash
mvn install:install-file \
   -Dfile=lib/opencv-342.jar \
   -DgroupId=opencv \
   -DartifactId=opencv \
   -Dversion=3.4.2 \
   -Dpackaging=jar 
