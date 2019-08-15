#!/bin/sh

BASEDIR=`dirname $0`
BASEDIR=`(cd "$BASEDIR"; pwd)`
echo current path $BASEDIR

INSTANCE="pid"
PIDPATH="$BASEDIR"

if [ "$1" != "" ]; then
    INSTANCE="$1"
fi

if [ "$2" != "" ]; then
    PIDPATH="$2"
fi

PIDFILE=$PIDPATH"/"$INSTANCE
echo $PIDFILE

if [ ! -f "$PIDFILE" ]
then
    echo "no registry to stop (could not find file $PIDFILE)"
else
    kill -9 $(cat "$PIDFILE")
    rm -f "$PIDFILE"
    echo STOPPED
fi
exit 0

echo stop finished.
