JoyQueue
====
JoyQueue 是一个兼容Kafka、MQTT协议的分布式、云原生消息平台。

## 主要特性：

* 发布/订阅消息模型
* 多消息协议兼容，如Kafka,MQTT
* 采用分区组(partition group)和分区(partitoin)语义
* 分区消息保证严格先进先出（FIFO）
* 支持生产/消费负载均衡
* 2PC 事务消息
* 支持Qos级别：ONE_WAY, RECEIVE,PERSISTENCE and REPLICATION
* 基于Raft选举的分区组复制
* 单分区千万级消息堆积
* 主题、分区组和分区可水平扩展
* 丰富的客户端支持，如Kafka,MQTT以及符合OMS（openmessaging）协议的客户端
* 完善的管理、配置和监控管理端

编译及安装
查看 [docs/cn/quickstart.md](docs/cn/quickstart.md).

参与贡献
====

JoyQueue 期待创建一个完善的消息平台社区，欢迎提出任何想法和问题

开源协议
====
遵循 Apache License, 版本 2.0:https://www.apache.org/licenses/LICENSE-2.0
