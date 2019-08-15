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
