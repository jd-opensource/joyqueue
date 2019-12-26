# JoyQueue | 快乐队列

[![Maven Central](https://img.shields.io/maven-central/v/org.joyqueue/joyqueue-client?color=brightgreen)](https://search.maven.org/search?q=org.joyqueue)
[![GitHub release](https://img.shields.io/badge/release-download-orange.svg)](https://github.com/chubaostream/joyqueue/releases)
![GitHub](https://img.shields.io/github/license/chubaostream/joyqueue)
![Travis CI](https://travis-ci.com/chubaostream/joyqueue.svg?branch=master)

[阅读中文版](docs/cn/README.md)

JoyQueue -- A cloud-native production-ready messaging platform with excellent performance. 

## Features

* High performance design with low latency and high throughput, see [performance](#Performance).
* Built-in multi-protocol support, working pretty well with OpenMessaging, Kafka and MQTT clients.
* easy to deploy: single-process, zero-dependent, no need of additional cluster coordination services.
* Large-scale clustering.
* Strong durability
* Consistent replication based on [Raft consensus algorithm](https://raft.github.io/).
* Full-featured web management console.
* Other nice features:
  * Rich metrics APIs
  * Transactions
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

We are dedicate to building high-quality messaging platform product. So any thoughts, pull requests, or issues are appreciated.
See [CONTRIBUTING.md](CONTRIBUTING.md) for details on submitting patches and the contribution workflow.

## License

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
