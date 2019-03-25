#!/bin/sh
if [ "$1" == "pid" ]
then
    PIDPROC=`cat ./jmq.pid`
else
    PIDPROC=`ps -ef | grep 'JMQLauncher' | grep -v 'grep'| awk '{print $2}'`
fi

if [ -z "$PIDPROC" ];then
 echo "jmq.server is not running"
 exit 0
fi

echo "PIDPROC: "$PIDPROC
for PID in $PIDPROC
do
if kill $PID
   then echo "process jmq.server(Pid:$PID) was force stopped at " `date`
fi
done
echo stop unfinished.
