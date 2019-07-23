# JoyQueue

[阅读中文版](docs/cn/README.md)

JoyQueue -- A cloud-native production-ready messaging platform with high-throughput.

## Features

* High performance design with low latency and high throughput, see [performance](#Performance).
* Buildin multiple protocols, compatible with OpenMessaging, Kafka and MQTT clients.
* Out of the box, easy to deploy: single-process, zero-dependent, no need to deploy additional ZooKeeper or Naming Service processes.
* Large-scale clustering.
* High availability, high reliability and auto recovery based on [Raft consensus algorithm](https://raft.github.io/).
* Full-featured web management console.
* More features:
  * Buildin metric APIs
  * Transaction
  * Parallel message consumption
  * Message archiving
  * Message preview
  * Automatically save consumption failure messages and error logs
  * Unlimited message stacking capacity

## Performance

A JoyQueue performance test results here：

| Scenario | QPS | clients | Delay AVG/TP99/TP999 (ms) | Fail(%)
| :----:| :----:|:----: |:----: |:----:|
|**Online service**| **510, 924** | 400| 1/4/8 | 0
|**Streaming** | **32, 961, 776** | 900 | N/A | 0

Scenario settings:

Scenario | Sync/Async | Batch | Compress | Msg size | Partitions | Client SDK
-- | -- | -- | -- | -- | -- | --
**Online service** | Sync | 1 | Not compressed | 1KB | 200 | joyqueue-client-4.1.0
**Streaming** | Async | 100 | LZ4 | 1KB | 200 | kafka-clients-2.1.1

For detail，visit [Performance](./docs/cn/performance.md)。

## Quickstart

Visit [Quick start](./docs/cn/quickstart.md)。

## Documentation

Visit [Documentation Index](./docs/cn/index.md)。

## Contributing

JoyQueue will continually dedicate to build a messaging platform community,any thoughts or issues are appreciated.

## License

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
