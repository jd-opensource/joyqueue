# 测试用例集

自动化集成测试用例集

### 测试环境要求

- 需要5台测试服务器，其中2台测试服务器，3台消息服务器，万兆以太网。  
- 消息服务器需要配备SSD磁盘，连续读写性能超过1GB/S。  
- 所有服务器软件需求：操作系统为Linux，安装好Docker环境。  
- 测试消息大小：大量消息大小在1KB左右，还需模拟少量不超过2MB的超大消息。

### 生产消费测试既性能测试
#### 测试目的
验证生产和消费的功能和性能是否符合预期。

#### 测试参数

Topic | Partition group | Partition | Producers | Consumers | 测试数据量
-- | -- | -- | -- | -- | --
test/1 | 3 | 50 | 10 | 50 | 2GB
test/2 | 6 | 100 | 50 | 100 | 10GB
test/3 | 6 | 200 | 50 | 200 | 120GB

#### 测试用例

序号 | Consumer | Producer |
-- | -- | -- 
1 | journalq | journalq 
2 | Kafka | Kafka 
3 | journalq2 | journalq2
4 | mqtt | mqtt
5 | journalq2 | journalq
6 | Kafka | journalq
7 | journalq | Kafka

#### 测试步骤

1. 创建Topic: test/1
2. 开启同步发送和消费；
1. 创建Topic： test/2
1. 开启异步发送和消费；
1. KILL一个Broker；
1. 等待30秒，验证：
    1. 客户端生产和发送正常不报错；
    1. 没有消费积压；
1. 启动一个Broker；
1. 等待10秒，验证：
    1. 客户端生产和发送正常不报错；
    1. 没有消费积压；
1. 创建Topic： test/3
1. 开启异步发送和消费；
1. 所有生产消费结束后，删除上述3个Topic；
1. 验证Topic： test/1和test/2功能：
    1. 收到消息数量 >= 生产消息数量；
1. 验证Topic： test/3功能：
    1. 收到消息数量 == 生产消息数量；
    1. 生产和消费没有报错；
    1. Broker日志没有打印异常信息；
1. 验证Topic: test/3的性能：
    1. 生产TPS：平均 > 20万，tp99 > 15万
    1. 消费TPS：平均 > 20万，tp99 > 15万

