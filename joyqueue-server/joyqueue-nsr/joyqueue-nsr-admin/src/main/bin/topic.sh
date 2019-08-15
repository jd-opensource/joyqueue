#!/bin/bash

if [ -d "./lib" ]; then
        CLASSPATH=$CLASSPATH:lib/*
else
    BASEDIR=`dirname $0`/../../..
    BASEDIR=`(cd "$BASEDIR"; pwd)`
    echo 'module dir:'$BASEDIR
    CLASSPATH=$BASEDIR/target/classes:`cat $BASEDIR/target/classpath.txt`
fi

JVM_MEM="-Xms4G -Xmx4G -XX:+UseG1GC"
JVM_GC_LOG=" -XX:+PrintGCDetails -XX:+PrintGCApplicationStoppedTime  -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=64m  -Xloggc:/dev/shm/benchmark-client-gc_%p.log"

java  -cp $CLASSPATH $JVM_MEM io.chubao.joyqueue.nsr.admin.TopicAdmin $*


