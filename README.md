JoyQueue
===
JoyQueue is a distributed,cloud-native messaging platform with high-throughput,high-availability and compatible with Kafka,MQTT.

### Main features as following:

* Pub/Sub messaging model
* Compatible with multiple protocols: kafka,mqtt,etc.
* Partition group and partition semantics
* Totally ordered in each partition(FIFO)
* Support produce/consume load balance
* 2PC transaction message
* Optional Qos levels: ONE_WAY, RECEIVE,PERSISTENCE and REPLICATION
* Raft-based partition group replication
* 10-million level message accumulation in single partition
* Horizontally scalable(topics,partition groups,partitions)
* support various clients,such as Kafka,MQTT,openmessaging client
* Well-designed administration,configuration and monitoring dashboard



Build and install
====
see [docs/en/quickstart.md](docs/en/quickstart.md).



Contributing
===
JoyQueue will continually dedicate to build a messaging platform community,any thoughts or issues are appreciated.


License
===
Licensed under the Apache License, Version 2.0:https://www.apache.org/licenses/LICENSE-2.0
