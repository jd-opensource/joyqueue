# 快乐队列 | JoyQueue

[View in English](../../README.md)  

**快乐队列** -- 云原生、高性能、企业级消息平台。

## 主要特性

* 兼顾低延时和高吞吐量的高性能设计，参见[性能](#%E6%80%A7%E8%83%BD);
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

## 性能

JoyQueue的一组性能测试数据：

| 场景 | QPS | 并发数量| 时延 AVG/TP99/TP999 (ms) | 失败率(%)
| :----:| :----:|:----: |:----: |:----:|
|**在线业务场景**| **510, 924** | 400| 1/4/8 | 0
|**流计算场景** | **32, 961, 776** | 900 | N/A | 0

测试场景的定义是：

场景 | 发送方式 | 批量大小 | 消息压缩方式 | 测试消息大小 | 分区数量 | 客户端
-- | -- | -- | -- | -- | -- | --
**在线业务场景** | 同步 | 1 | 不压缩 | 1KB | 200 | joyqueue-client-4.1.1
**流计算场景** | 异步 | 100 | LZ4 | 1KB | 200 | kafka-clients-2.1.1

详细的测试情况，请查看[性能](./performance.md)。

## 快速开始

查看[快速开始](./quickstart.md)。

## 文档

查看[文档目录](./index.md)。

## 参与贡献

JoyQueue 期待创建一个完善的消息平台社区，欢迎提出任何想法和问题

## 开源协议

遵循 Apache License, 版本 2.0:https://www.apache.org/licenses/LICENSE-2.0
