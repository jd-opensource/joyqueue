#!/bin/sh

BASEDIR=`dirname $0`/..
BASEDIR=`(cd "$BASEDIR"; pwd)`
default_log_file='info.log'
if [ -z $LOGFILE ];then
    LOGFILE=$BASEDIR/$default_log_file
fi
echo "log file:$LOGFILE"
exec $(dirname $0)/server-start.sh "$@" >$LOGFILE 2 >& 1


