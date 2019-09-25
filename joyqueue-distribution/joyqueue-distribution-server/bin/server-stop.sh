#!/bin/sh
#
# Copyright 2019 The JoyQueue Authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


if [ "$1" == "pid" ]
then
    PIDPROC=`cat ./joyqueue.pid`
else
    PIDPROC=`ps -ef | grep 'Launcher' | grep -v 'grep'| awk '{print $2}'`
fi

if [ -z "$PIDPROC" ];then
 echo "joyqueue server is not running"
 exit 0
fi

echo "PIDPROC: "$PIDPROC
for PID in $PIDPROC
do
if kill $PID
   then echo "process joyqueue server (Pid:$PID) was force stopped at " `date`
fi
done
echo stop unfinished.
