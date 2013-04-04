#!/bin/bash

DIR_NAME=`dirname $0`

CLASSPATH=""

for l in `find $DIR_NAME/../../../build/libs/ -name "*jar"`; do CLASSPATH=${CLASSPATH}:$l ; done

java -server -Dlogback.configurationFile=logback.xml -cp $CLASSPATH org.openiec61850.StandaloneServer $DIR_NAME/sampleServer.properties
