#!/bin/bash
echo "脚本$0"
echo "开始序号$1"
echo "结束序号$2"
echo "broker.id:$3"

start=$1
end=$2
brokerId=$3
while(( $start<=$end ))
do
    topic=journalq@test$start
    echo $topic
    curl -X POST -H "Content-Type: text/plain" --data '{"topic":{"code":"'$topic'","partitions":5,"type":0,"name":"test","priorityPartitions":[1]},"partitionGroups":[{"topic":"'$topic'","leader":'$brokerId',"term":0,"isr":[],"learners":[],"group":1,"partitions":[1,2,3],"replicaGroups":['$brokerId'],"electType":"fix"}]}' http://localhost:8080/topic/add
    let "start++"
done
