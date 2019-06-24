JoyQueue
===
JoyQueue is a distributed and cloud-native message platform with high-throughput,high-availability and compatible with Kafka,MQTT.

### Main features as following:

* Pub/Sub messaging model
* Compatible with multiple protocols: kafka,mqtt etc
* Partition group and partition semantics
* Totally ordered in each partition(FIFO)
* Support produce/consume load balance
* 2PC transaction message
* Optional Qos levels: UNRELIABLE, LOCAL_RELIABLE,CLUSTER_RELABLE
* Raft-based partition group replication
* Million level message accumulation in single partition
* Horizontally scalable(topics,partiton groups,partitions)
* support various clients,such as Kafka,MQTT,open messaging client
* Well-designed Administration,configuration and monitoring dashboard



Build and install
====
see [docs/en/quickstart.md](docs/en/quickstart.md).



Contributing
===
JoyQueue will continuely dedicate to build a messageing platform community, any thoughts or issues are  appreciated.


License
===
Licensed under the Apache License, Version 2.0:https://www.apache.org/licenses/LICENSE-2.0
