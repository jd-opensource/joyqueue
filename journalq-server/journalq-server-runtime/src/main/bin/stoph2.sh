#!/bin/sh
if [ "$1" == "pid" ]
then
    PIDPROC=`cat ./h2.pid`
else
    PIDPROC=`ps -ef | grep 'org.h2.tools.Server' | grep -v 'grep'| awk '{print $2}'`
fi

if [ -z "$PIDPROC" ];then
 echo "h2.server is not running"
 exit 0
fi

echo "PIDPROC: "$PIDPROC
for PID in $PIDPROC
do
if kill $PID
   then echo "process h2.server(Pid:$PID) was force stopped at " `date`
fi
done
echo stop unfinished.
