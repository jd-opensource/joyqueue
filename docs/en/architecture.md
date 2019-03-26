# Architecture overview
JournalQ is a multi-protocols pub/sub broker,consist of several modules,including Protocol,Poducer,Consumer,Storage,Replication and Naming et.

Generally,One topic consist of several partition groups,which contains multiple partitions. And parition group of the topic will distribute among more than one brokers,which is helpful for Horizontally scale. partion group cluster consit of the same parttion group on different broker, only the leader partiton group can handle produce/consume message requests.

Naming service keeps topic,app and pub/sub metadata,such as pub/sub relationship between topic and app. The metadata replicates among JournalQ instances.

Key modules of JournalQ are showed in below:

![JournalQ highlight](../img/journalQ-arch-cluster.png "JournalQ architecture")

## Broker
JournalQ instance is responsible for messages store,produce and consume. In addition,Archive,retry,HA and full-featured monitor are provided for production ready.

Retry module enable consumer to skip the message temporary and consume it later.
JournalQ instance's storage is always limited by disk space, archive module can archive the consumed message to a larger storage,such as HDFS,with clean up strategies,and then you may look up and download a message which is produced a month ago. HA module manages followers and replicates messages to them, and a new leader will be elected among alive followers when current leader crashed.Raft-based election algorithm is employed by JournalQ.

Monitor module manages real-time monitoring data,such as pending,retry,enqueue, dequeue and connection,which is intutive and helpful to know what happening.


## Naming Cluster

Naming service enable every JorunalQ instance can provide service discovery,and it's a embedded ignite,which replicates metadata among cluster instances.  

## Producer Cluster

Producer may be interested in one or more topics,and several Producers could have the same topic. Distributed producers could employ default or self-defined partition selector to load balance messages to broker cluster.

## Consumer Cluster

Consumer may be interested in one or more topics,and several Consumers could have the same topic, JournalQ supports group consume or broadcast.Group consume indicates that per message will only be consumed by one of consumer in the group,JournalQ adopted a load balance algorithm to coordinate   consumer to target partition. Broadcast means all the consumer will consume the complete messages of the topic, which implements by control consume offset on local.
