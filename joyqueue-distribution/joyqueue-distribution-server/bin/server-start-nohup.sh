#!/bin/sh

exec nohup $(dirname $0)/server-start.sh "$@" >/dev/null 2>/dev/null &
echo $! > joyqueue.pid