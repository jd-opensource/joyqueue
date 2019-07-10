# JoyQueue

[View in English](../../README.md)  

**JoyQueue** -- 云原生、高性能、企业级消息队列。

在京东，JoyQueue部署在跨越全球多个IDC中，上千个实例的超大规模集群，支撑上千个应用，每日数千亿消息，平均响应时延不超过1毫秒。

## 主要特性

* 兼顾低延时和高吞吐量的高性能设计，参见[性能测试](Performance.md);
* 兼容多种协议，支持[OpenMessaging](https://github.com/openmessaging/openmessaging-java)，[Kafka](http://kafka.apache.org/)和[MQTT](http://mqtt.org/)客户端，支持异构客户端生产或消费同一Topic；
* 开箱即用，易于部署：单进程、零依赖，无需部署额外的ZooKeeper或Naming Service。
* 支持超大规模集群部署和弹性扩容；
* 基于[Raft](https://raft.github.io/)实现的高可用、高可靠设计。集群节点宕机时不停服，不丢消息；
* 功能完善的Web管理端；
* 完备的企业级功能：
  * 完善的性能监控API；
  * 完整的事务支持；
  * 并行消费；
  * 消息归档、消息预览；
  * 自动保存消费失败的消息和错误日志；
  * 无限的消息堆积能力；

## 编译及安装
查看 [docs/cn/quickstart.md](docs/cn/quickstart.md).

## 性能

## 参与贡献

JoyQueue 期待创建一个完善的消息平台社区，欢迎提出任何想法和问题

## 开源协议

遵循 Apache License, 版本 2.0:https://www.apache.org/licenses/LICENSE-2.0
