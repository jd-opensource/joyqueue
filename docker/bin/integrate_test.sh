#!/bin/sh

# start to test

python3 ./integration/bootstrap.py $*
while getopts "s:b::" opt; do
  case $opt in
    s) score=$OPTARG;;
  esac
done
if [ -e $score/score.json ];then
    exit 0
else
    exit 1
fi
echo 'rm log'
if [ -f 'integration/log.txt' ]; then
    rm integration/log.txt
fi
