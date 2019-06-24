http://localhost:8080//topic/add
{"topic":{"code":"joyqueue@test","partitions":5,"type":0,"name":"test","priorityPartitions":[1]},"partitionGroups":[{"topic":"joyqueue@test","leader":-1,"term":0,"isr":[1,2],"learners":[1,2],"group":1,"partitions":[1,2,3],"replicaGroups":[1,2],"electType":"fix"},{"topic":"joyqueue@test","leader":-1,"term":0,"isr":[1,2],"learners":[1,2],"group":1,"partitions":[4,5],"replicaGroups":[1,2],"electType":"fix"}]}

http://localhost:8080/topic/remove
{"topic":{"code":"joyqueue@test"}


http://localhost:8080/topic/addPartitionGroup
{"topic":"joyqueue@test","leader":-1,"term":0,"isr":[1,2],"learners":[1,2],"group":1,"partitions":[1,2,3],"replicaGroups":[1,2],"electType":"fix"}

http://localhost:8080/topic/updatePartitionGroup
{"topic":"joyqueue@test","leader":-1,"term":0,"isr":[1,2],"learners":[1,2],"group":1,"partitions":[1,2,3],"replicaGroups":[1,2],"electType":"fix"}

http://localhost:8080/topic/removePartitionGroup
{"topic":"joyqueue@test","group":1}

http://localhost:8080/producer/add
{"app":"aa","topic":"joyqueue@test","clientType":"mqtt","producerPolicy":{"nearby":true,"single":true,"archive":true,"timeOut":10000,"blackList":["192.168.1.1"]}}


http://localhost:8080/producer/update
{"app":"aa","topic":"joyqueue@test","clientType":"mqtt","producerPolicy":{"nearby":true,"single":true,"archive":true,"timeOut":10000,"blackList":["192.168.1.1"]}}

http://localhost:8080/producer/remove
{"app":"aa","topic":"joyqueue@test"}


http://localhost:8080/consumer/add
{"app":"aa","topic":"joyqueue@test","clientType":"mqtt","consumerPolicy":{"nearby":true,"paused":true,"archive":true,"retry":true,"seq":true,"ackTimeout":1000,"batchSize":100,"concurrentConsume":false,"concurrentPrefetchSize":100,"delay":100,"errTimes":100,"blackList":["192.168.1.1"]},"retryPolicy":{"maxRetrys":10,"maxRetryDelay":10,"retryDelay":1280000,"expireTime":1202312}}

http://localhost:8080/consumer/update
{"app":"aa","topic":"joyqueue@test","clientType":"mqtt","consumerPolicy":{"nearby":true,"paused":true,"archive":true,"retry":true,"seq":true,"ackTimeout":1000,"batchSize":100,"concurrentConsume":false,"concurrentPrefetchSize":100,"delay":100,"errTimes":100,"blackList":["192.168.1.1"]},"retryPolicy":{"maxRetrys":10,"maxRetryDelay":10,"retryDelay":1280000,"expireTime":1202312}}

http://localhost:8080/consumer/remove
{"app":"aa","topic":"joyqueue@test"}


****************************************************************************************
-- add topic
curl -X POST -H "Content-Type: text/plain" --data '{"topic":{"code":"joyqueue@test","partitions":5,"type":0,"name":"test","priorityPartitions":[1]},"partitionGroups":[{"topic":"joyqueue@test","leader":1540288307,"term":0,"isr":[],"learners":[],"group":1,"partitions":[1,2,3],"replicaGroups":[1540288307],"electType":"fix"},{"topic":"joyqueue@test","leader":1540288307,"term":0,"isr":[],"learners":[],"group":1,"partitions":[4,5],"replicaGroups":[1540288307],"electType":"fix"}]}' http://localhost:8080/topic/add

-- add producer
curl -X POST -H "Content-Type: text/plain" --data '{"app":"aa","topic":"joyqueue@test","clientType":"joyqueue2","producerPolicy":{"nearby":false,"single":false,"archive":false,"timeOut":10000,"blackList":["192.168.1.1"]}}' http://localhost:8080/producer/add

-- add consumer
curl -X POST -H "Content-Type: text/plain" --data '{"app":"aa","topic":"joyqueue@test","clientType":"joyqueue2","consumerPolicy":{"nearby":false,"paused":false,"archive":false,"retry":false,"seq":false,"ackTimeout":1000,"batchSize":100,"concurrentConsume":false,"concurrentPrefetchSize":100,"delay":0,"errTimes":100,"blackList":["192.168.1.1"]},"retryPolicy":{"maxRetrys":10,"maxRetryDelay":10,"retryDelay":1280000,"expireTime":1202312}}' http://localhost:8080/consumer/add

