#!/bin/bash
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


function help()
{
echo "    usage: topic.sh <[options]>
    options:
        -c   create|publish|subscribe|token command
        -a   application
        -t   topic
        -b   broker id
        -p   partitions
        -k   token
        -h   help
    description: create/pub/sub topic and generate token for application "
    exit;
}

function checkTopicAndApp(){
   if [[ -z $TOPIC||-z $APP ]];then
     echo "topic and app can not be null ";
     exit 1;
   fi
}


# echo "this is -a the arg is ! $OPTIND $OPTARG"

while getopts "c:t:a:b:p:hr::" opt; do
  case $opt in
    c)cmd=$OPTARG;;
    t)TOPIC=$OPTARG;;
    k)TOKEN=$OPTARG;;
    a)APP=$OPTARG;;
    b)BROKER=$OPTARG;;
    p)PARTITIONS=$OPTARG;;
    r)
     if [ -n $OPTARG ]; then
          REMOTE=$OPTARG;
     else
         REMOTE="localhost"
     fi
     ;;
    h) help;;
  esac
done
if [ -z "$cmd" ]; then
   help;
   exit;
fi

if [[ $cmd -eq "create" && -z "$PARTITIONS" ]]; then
      PARTITIONS=5
fi

# echo $TOPIC,$BROKER,$PARTITIONS

case $cmd in
     create)
        if [ -z "$TOPIC" ];then
         TOPIC="test_topic";
         echo "use default topic: test_topic";
        fi
        HTTP_RESPONSE=$(curl --silent --write-out "HTTP_STATUS:%{http_code}" -X POST -H "Content-Type: text/plain" -d'{"topic":{"name":{"code":"'$TOPIC'","fullName":"'$TOPIC'","namespace":""},
          "partitions":'$PARTITIONS',"type":"TOPIC","priorityPartitions":[1]},"partitionGroups":[{"leader":-1,"term":0,"isr":[],
          "learners":[],"group":0,"partitions":[0,1,2,3,4],"replicas":['$BROKER'],"electType":"raft","topic":{"code":"'$TOPIC'","fullName":"'$TOPIC'","namespace":""}}]}' "http://$REMOTE:50091/topic/add")
        ;;
     publish)
        checkTopicAndApp;
        HTTP_RESPONSE=$(curl --silent --write-out "HTTP_STATUS:%{http_code}" -X POST -H "Content-Type: text/plain" -d '{"app":"'$APP'","topic":{"code":"'$TOPIC'","fullName":"'$TOPIC'","namespace":""},"clientType":"JMQ","producerPolicy":
        {"nearby":false,"single":false,"archive":false,"timeOut":10000,"blackList":[]}}' http://$REMOTE:50091/producer/add)
        ;;
     subscribe)
        checkTopicAndApp;
        HTTP_RESPONSE=$(curl --silent --write-out "HTTP_STATUS:%{http_code}" -X POST -H "Content-Type: text/plain" -d '{"app":"'$APP'","topic":{"code":"'$TOPIC'","fullName":"'$TOPIC'","namespace":""},"clientType":"JMQ","consumerPolicy":
            {"nearby":false,"paused":false,"archive":false,"retry":false,"seq":false,"ackTimeout":10000,"batchSize":100,"concurrentConsume":false,"concurrent":0,"delay":100,"errTimes":100,"blackList":[],
            "maxPartitionNum":3,"errTimes":3,"readRetryProbability":20},"retryPolicy":{"maxRetrys":10,"maxRetryDelay":10,"retryDelay":1280000,"expireTime":1202312}}' http://$REMOTE:50091/consumer/add)
        ;;
     token)
        if [ -z "$APP" ];then
              echo "please provide a app";
              exit 1;
        fi
        if [ -z "$TOKEN" ];then
            TOKEN="test_token"
            echo "use default token:$TOKEN";
        fi
        t1=`date  +%s`000;
        year=31104000000;
        t2=`expr $t1 + $year`;
        id=$RANDOM
        # echo $t1,$t2,$id
        HTTP_RESPONSE=$(curl --silent --write-out "HTTP_STATUS:%{http_code}" -X POST -H "Content-Type: text/plain" -d '{"id":'$id',"app":"'$APP'","token":"'$TOKEN'","effectiveTime":'$t1',"expirationTime":'$t2'}' http://$REMOTE:50091/apptoken/add)
        ;;
     *)
        echo "un support command"
        exit 1
        ;;
esac
HTTP_BODY=$(echo $HTTP_RESPONSE | sed -e 's/HTTP_STATUS\:.*//g')
echo  $HTTP_BODY










