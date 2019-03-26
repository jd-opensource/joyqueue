# Architecture overivew
JorunalQ is a multi-protocols pub/sub broker,consist of several modules,including Protocol,Poducer,Consumer,Storage,Replication and Naming et.

Generally,One topic consist of several partition groups,which contains multiple partitions. And parition group of the topic will distribute among more than one brokers,which is helpful for Horizontally scale. partion group cluster consit of the same parttion group on different broker, only the leader partiton group can handle produce/consume message requests.



![JournalQ highlight](../img/journalQ-arch-cluster.png "JournalQ architecture")

## Broker

## Naming Service

## Broker Cluster

## Producer Cluster

## Consumer Cluster
