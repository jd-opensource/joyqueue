# 目录

* [基本概念](#基本概念)
* [整体流程](#整体流程)
* [协调者](#协调者)
* [事务消息](#事务消息)
* [通信协议](#通信协议)
	* [基本类型](#基本类型)
	* [头信息](#头信息)
	* [通用相关](#通用相关)
		* [ADD_CONNECTION](#ADD_CONNECTION)
		* [REMOVE_CONNECTION](#REMOVE_CONNECTION)
		* [HEARTBEAT](#HEARTBEAT)
		* [FETCH_HEALTH](#FETCH_HEALTH)
	* [元数据相关](#元数据相关)
		* [FETCH_CLUSTER](#FETCH_CLUSTER)
	* [协调者相关](#协调者相关)
		* [FIND_COORDINATOR](#FIND_COORDINATOR)
		* [FETCH_ASSIGNED_PARTITION](#FETCH_ASSIGNED_PARTITION)
	* [消费相关](#消费相关)
		* [ADD_CONSUMER](#ADD_CONSUMER)
		* [REMOVE_CONSUMER](#REMOVE_CONSUMER)
		* [FETCH_TOPIC_MESSAGE](#FETCH_TOPIC_MESSAGE)
		* [FETCH_PARTITION_MESSAGE](#FETCH_PARTITION_MESSAGE)
		* [COMMIT_ACK](#COMMIT_ACK)
		* [FETCH_INDEX](#FETCH_INDEX)
	* [生产相关](#生产相关)
		* [ADD_PRODUCER](#ADD_PRODUCER)
		* [REMOVE_PRODUCER](#REMOVE_PRODUCER)
		* [PRODUCE_MESSAGE](#PRODUCE_MESSAGE)
		* [PRODUCE_MESSAGE_PREPARE](#PRODUCE_MESSAGE_PREPARE)
		* [PRODUCE_MESSAGE_COMMIT](#PRODUCE_MESSAGE_COMMIT)
		* [PRODUCE_MESSAGE_ROLLBACK](#PRODUCE_MESSAGE_ROLLBACK)
		* [FETCH_PRODUCE_FEEDBACK](#FETCH_PRODUCE_FEEDBACK)
* [命令目录](#命令目录)
* [错误码](#错误码)

## 基本概念

jmq4是基于拉模型的消息中间件，采用TCP协议提供服务

jmq4服务端提供负载均衡（不是严格的均衡），由不同的协调者处理，用于协调消费者可消费哪些分区

几个基本概念:

* broker：集群中的服务器
* namespace：例：泰国，印尼等
* region：例：广州，宿迁等
* app（应用）：指不同的生产和消费组
* topic（主题）：指不同的主题，每个主题中的消息独立处理
* partition（分区）：topic上的最小处理单元，每个partition上消息有序，每个partition上的每个消息会分配连续递增的index
* index （索引）：指消息在partition上的位置
* message（消息）：发送的基本单位
* consumer（消费者）：消息的消费者
* producer（生产者）：消息的生产者

## 整体流程

客户端和任意broker建立连接（由我们提供的域名），连接建立后获取相关元数据，根据元数据信息连接到相应broker进行生产和消费

对于就近生产和消费，元数据会根据建立连接时提供的region对broker做标识，客户端可以根据标识判断是否在同一个region内

### 连接流程

建立连接后，客户端需要在第一个命令之前发送ADD_CONNECTION命令，通知服务端做数据初始化和监控

在断开连接前，客户端需要发送REMOVE_CONNECTION命令，通知服务端连接将要断开

### 元数据

通过FETCH_CLUSTER获取相关topic元数据，元数据内包括topic相关的所有partitionGroup，partition，broker，消费和生产策略信息

如果有获取元数据失败或没有订阅关系等错误，会通过错误码返回，由客户端自行处理

客户端需要对元数据做缓存和定期刷新，还需要根据错误码主动刷新，以减少和服务端的交互次数

### 生产流程

在生产前，需要通过ADD_PRODUCER命令通知服务端将要生产的topic，每个连接的每个topic只需要一次，便于服务端监控

通过PRODUCE_MESSAGE，PRODUCE_MESSAGE_PREPARE，PRODUCE_MESSAGE_COMMIT，PRODUCE_MESSAGE_ROLLBACK命令生产消息

PRODUCE_MESSAGE命令可以接受多topic批量生产，单topic批量生产，并一次性返回结果

服务端根据服务级别决定什么时候返回给客户端响应，例：ACK_RECEIVE：在服务端收到响应后响应，ACK_WRITE：在服务端刷盘后响应

需要客户端对消息进行压缩处理，提升网络传输效率，减少服务端存储占用

### 消费流程

在消费前，需要通过ADD_CONSUMER命令通知服务端将要消费的topic，每个连接的每个topic只需要一次，便于服务端监控

通过FETCH_TOPIC_MESSAGE和FETCH_PARTITION_MESSAGE拉取消费数据，这两个命令可以满足大部分的消费场景

拉取消息后通过COMMIT_ACK提交ACK，通知服务端已经消费成功，避免重复消费

对于广播消费服务端没有直接支持，可以通过FETCH_PARTITION_MESSAGE命令，由客户端自己管理index实现（这种实现没有服务端重试，需要客户端自己处理）

客户端需要根据消息体内的压缩类型做消息解压处理

客户端需要对消费时的错误码进行处理，抛出异常或主动刷新元数据等处理

如果客户端需要负载均衡，可以使用服务端提供的协调者来处理，可以减少开发量，服务端也会提供相应的监控信息

## 事务消息

jmq4支持事务消息，整体流程类似二阶段提交

首先客户端发起prepare命令，向服务端传递本次事务的主题，应用，事务id（用户自定义的），事务超时等，服务端会返回相应的txId（事务id）

等待prepare响应后使用produce命令发送消息，并带上prepare的txId

最后发送commit或rollback命令结束本次事务

在事务过期后 (默认十分钟)，未提交和回滚的事务可以由客户端主动拉取到，可以做事务补偿

## 协调者

协调者用于负载均衡，每个topic和app的组合会有不同的协调者，协调者也是一个broker

需要向任意一个broker发送FIND_COORDINATOR命令来确定协调者是哪个broker，然后向协调者发送FETCH_ASSIGNED_PARTITION命令获取分配情况，然后再根据分配情况进行消费

协调者不是必须的，可以绕过协调者自己调整消费分配

## 通信协议

### 基本类型

基本类型：

类型|字节|变长|描述
-|-|-|-
BYTE|1|N|byte类型
BOOLEAN|1|N|boolean类型
SHORT|2|N|short类型
INT|4|N|int类型
LONG|8|N|long类型
DOUBLE|8|N|double类型
STRING|2|Y|string类型，变长字段，前两字节长度，长度内的为内容（长度内不包括长度的两个字节）
ARRAY|2|Y|表示数组，前两个字节表示数组长度
BYTES|4|Y|表示字节数组，前四个字节表示长度，长度内的为内容 （长度内不包括长度的四个字节）

MESSAGE（消息格式）：

systemCode需要位运算，第一位是否压缩，第二位是否顺序消息，第三，四位消息来源 (0：jmq，1：kafka，2：mqtt，3：joyqueue，4：其他)，第五，六，七位压缩类型（0：snappy，1：zip，2：zlib），第八位地址类型，0 ipv4，1 ipv6，第九位，十，十一，十二消息版本号，固定值1，第十三位是否批量消息，第十四，十五，十六位预留

名称|类型|描述
-|-|-
length|INT|消息总体长度
partition|SHORT|分区
index|LONG|消息索引
term|INT|任期
systemCode|SHORT|系统码
priority|BYTE|消息优先级
sendTime|LONG|发送时间
storeTime|INT|存储相对时间
bodyCRC|LONG|body crc校验码
flag|SHORT|如果是批量消息表示批量消息数量，否则表示flag
body|BYTES|消息体
businessId|STRING|业务id
attributes|ATTRIBUTE|属性
extension|BYTES|扩展内容
app|STRING|应用

ATTRIBUTE：

前两个字节是长度，后续跟内容

properties格式内容，\n换行，=分割kv

BATCH_MESSAGE (批量消息格式)：

批量消息是以一条消息处理的，只是把批量内的所有消息内容保存在body，其他格式和普通消息一致

body格式
	
	length => INT 批量消息总体长度
	messages => ARRAY
		length => INT 单条消息长度
		priority => BYTE 消息优先级
		flag => SHORT flag
		body => BYTES 消息体
		businessId => BYTES 业务id
		attributes => ATTRIBUTES 属性

### 头信息

目前magic为固定值0xCAFEBEBE, version为固定值2

identity一个字节，位运算，第一位是请求类型 （0：请求，1：响应）, 第二，三位是qosLevel（0：ACK_FLUSH，1：ACK_RECEIVE，2：ACK_NO，3：ACK_WRITE）

没有回复的命令由status和error传递回复

名称|类型|可选|描述
-|-|-|-
length|INT|N|整个payload长度，包括长度的4字节
magic|INT|N|magic，固定值0xCAFEBEBE
version|BYTE|N|版本号
identity|byte|N|相当于flag，里面包括qosLevel，请求类型
requestId|INT|N|请求id，要求递增
type|BYTE|N|命令类型
sendTime|LONG|N|发送时间
status|BYTE|Y|错误码，只有response需要传递
error|STRING|Y|错误信息，只有response需要传递，可以为空

### 通用相关

#### ADD_CONNECTION

添加一个连接，用于数据初始化和监控，是建立连接后的第一个请求

请求:

名称|类型|可为空值|描述
-|-|-|-
username|STRING|Y|用户名，保留字段
password|STRING|Y|密码，保留字段
app|STRING|N|应用名
token|STRING|N|令牌
region|STRING|Y|区域
namespace|STRING|Y|命名空间
version|STRING|N|客户端版本，可以自定义值
ip|STRING|N|客户端ip
time|LONG|N|连接时间
sequence|LONG|N|序号，用于标记是客户端的第几次连接

响应:

	connectionId => STRING 连接id，对客户端没有具体作用
	notification => STRING 通知，服务端可能返回一些通知信息，需要以日志或其他形式通知用户

#### REMOVE_CONNECTION

移除一个连接，在断开连接前发送

请求：

	无

响应：

	无

#### HEARTBEAT

心跳，用于保持连接

请求：

	无

响应：

	无

#### FETCH_HEALTH

拉取broker健康度，可以根据broker健康度对broker降权等

请求：

	无

响应：

	point => DOUBLE 健康分数


### 元数据相关

#### FETCH_CLUSTER

拉取app相关topic的集群信息

注意，topic里的partitionGroup的leader是id，需要通过解析broker列表后通过brokerId进行匹配

请求：

	topics => ARRAY
		topic => STRING 主题
	app => STRING 应用

响应：

	topics => ARRAY
		topicCode => STRING 主题名称
		isExistProducerPolicy => 是否存在生产策略，也就是是否有生产的订阅关系，如果不存在没有producerPolicy的内容
		producerPolicy => 生产策略
			isNearby => BOOLEAN 是否就近生产
			isSingle => BOOLEAN 是否单线程发送
			isArchive => BOOLEAN 是否归档
			weight => ARRAY 权重
				brokerId => STRING brokerId
				weight => SHORT 权重值
			blackList => ARRAY 黑名单
				ip => 禁止生产的ip
			timeout => INT 生产超时
		isExistConsumerPolicy => 是否存在消费策略，也就是是否有消费的订阅关系，如果不存在没有consumerPolicy的内容
		consumerPolicy => 消费策略
			isNearby => BOOLEAN 是否就近消费
			isPaused => BOOLEAN 是否暂停消费
			isArchive => BOOLEAN 是否归档
			isRetry => BOOLEAN 是否重试
			isSeq => BOOLEAN 是否顺序消费
			ackTimeout => INT 应答超时
			batchSize => SHORT 批量大小
			concurrentConsume => BOOLEAN 是否并行消费
			concurrentPrefetchSize => INT 并行消费预读数量
			delay => INT 延迟时间
			blackList => ARRAY 黑名单
				ip => STRING 禁止消费的ip
			errTimes => INT 进入重试的错误次数
			maxPartitionNum => INT 一个连接占用的分区数
			readRetryProbability => INT 读取重试队列的比例
		type => INT topic类型，-1 无类型，0 普通主题，1 广播，2 顺序主题
		partitionGroups => ARRAY
			id => INT partitionGroupId
			leader => INT leader brokerId
			partitions => ARRAY
				id => INT partitionId
		code => INT 错误码，不是SUCCESS都表示错误
	brokers => ARRAY
		id => INT brokerId
		host => STRING brokerHost
		port => INT brokerPort
		dataCenter => STRING 数据中心
		nearby => BOOLEAN 是否就近
		weight => INT 权重

### 协调者相关

#### FIND_COORDINATOR

查找协调者信息，可以发送给集群内任意broker

请求：

	topics => ARRAY
		topic => STRING
	app => STRING 应用

响应：

	coordinators => ARRAY
		topic => STRING 主题
		id => INT 协调者brokerid，如果为-1表示没有协调者，下面也没有broker相关字段
		host => STRING 协调者broker host
		port => INT 协调者broker port
		dataCenter => STRING 协调者broker数据中心
		isNearby => BOOLEAN 协调者broker是否在同一区域
		weight => INT 协调者broker权重
		code => INT 错误码

#### FETCH_ASSIGNED_PARTITION

拉取分配情况

请求：
	
	topics => ARRAY
		topic => STRING 主题
		sessionTimeout => INT 会话超时，在超时后会释放相应的分配
		isNearby => BOOLEAN 是否就近分配
	app => STRING 应用

响应：

	data => ARRAY
		topic => STRING 主题
		partitions => ARRAY
			partition => SHORT 分配的分区
		code => INT 错误码


### 消费相关

#### ADD_CONSUMER

添加消费者，在消费相应主题前发送

请求：

	topics => ARRAY
		topic => STRING 主题
	app => STRING 应用
	sequence => LONG 递增序号

响应：

	consumerIds => ARRAY
		topic => STRING 主题
		consumerId => STRING 消费者id，没有具体用途

#### REMOVE_CONSUMER

移除消费者，不需要消费或断开连接时发送

请求：

	topics => ARRAY
		topic => STRING 主题
	app => STRING 应用

响应：

	无

#### FETCH_TOPIC_MESSAGE

拉取主题消息，由服务端控制拉取的分区和index

请求：

	topics => ARRAY
		topic => STRING 主题
		count => SHORT 拉取数量
	app => STRING 应用
	ackTimeout => INT ack超时
	longPollTimeout => INT 长轮询超时

响应：

	data => ARRAY
		topic => STRING 主题
		messages => ARRAY(MESSAGE)

#### FETCH_PARTITION_MESSAGE

拉取消息，可决定拉取的主题和分区还有index

请求：

	topics => ARRAY
		topic => STRING 主题
		partitions => ARRAY
			partition => SHORT 分区
			count => INT 拉取数量
			index => LONG 索引，-1为ack索引
	app => STRING 应用

响应：

	data => ARRAY
		topic => STRING 主题
		partitions => ARRAY
			partition => SHORT 分区
			messages => ARRAY(Message)
			code => INT 错误码

#### COMMIT_ACK

提交ack，需指定每个ack消息的信息

请求：

	topics => ARRAY
		topic => STRING 主题
		partitions => ARRAY
			partition => SHORT 分区
			data => ARRAY
				partition => SHORT 分区
				index => LONG 索引
				type => BYTE 类型，0：空，1：超时，2：异常，3：其他，除空外其他类型会做重试处理
	app => STRING 应用

响应：

	data => ARRAY
		topic => STRING 主题
		partitionData => ARRAY
			partition => SHORT 分区
			code => INT 错误码

#### FETCH_INDEX

拉取ack index

请求：

	topics => ARRAY
		topic => STRING 主题
		partitions => ARRAY
			partition => SHORT 分区
	app => STRING 应用

响应：

	topics => ARRAY
		topic => STRING 主题
		partitions => ARRAY
			partition => SHORT 分区
			index => LONG 索引
			code => INT 错误码

### 生产相关

#### ADD_PRODUCER

添加生产者，在生产相应主题前发送

请求：

	topics => ARRAY
		topic => STRING 主题
	app => STRING 应用
	sequence => LONG 递增序号

响应：

	producerIds => ARRAY
		topic => STRING 主题
		producerId => STRING 生产者id，没有具体用途

#### REMOVE_PRODUCER

移除生产者，不需要生产或断开连接时发送

请求：

	topics => ARRAY
		topic => STRING 主题
	app => STRING 应用

响应：

	无

#### PRODUCE_MESSAGE

生产消息

请求：

	topics => ARRAY
		topic => STRING 主题
		txId => 事务id
		timeout => 生产超时
		qosLevel => BYTE 服务水平，0：ACK_FLUSH，1：ACK_RECEIVE，2：ACK_NO，3：ACK_WRITE
		messages => ARRAY(MESSAGE)
	app => STRING 应用

响应：
	
	data => ARRAY
		topic => STRING 主题
		code => INT 错误码
		result => ARRAY
			partition => SHORT 分区
			index => LONG 索引
			startTime => LONG 开始处理时间

#### PRODUCE_MESSAGE_PREPARE

生产事务消息prepare

请求：

	topic => STRING 主题
	app => STRING 应用
	sequence => LONG 序列号，表示客户端的第几个事务
	transactionId => STRING 用户自定义的事务id，用于补偿事务

响应：

	txId => STRING 事务id
	code => INT 错误码

#### PRODUCE_MESSAGE_COMMIT

提交事务

请求：

	topic => STRING 主题
	app => STRING 应用
	txId => 事务id

响应：

	code => INT 错误码

#### PRODUCE_MESSAGE_ROLLBACK

回滚事务

请求：

	topic => STRING 主题
	app => STRING 应用
	txId => 事务id

响应：

	code => INT 错误码

#### FETCH_PRODUCE_FEEDBACK

拉取补偿，用于事务补偿，只有自定义了transactionId的事务才能拉取到

请求：

	app => STRING 应用
	topic => STRING 主题
	status => BYTE 状态，保留字段，0：UNKNOWN，1：PREPARE，2：COMMITTED，3：ROLLBACK
	count => INT 拉取数量
	longPollTimeout => LONG 长轮询超时，保留字段

响应：

	data => ARRAY
		topic => STRING 主题
		txId => STRING 事务id
		transactionId => STRING 自定义事务id
	code => INT 错误码
	
## 命令目录

没有回复的命令没有具体回复，可以根据header判断是否成功

code|name|描述
-|-|-
1|ADD_CONNECTION|添加连接
-1|ADD_CONNECTION_ACK|添加连接回复
2|REMOVE_CONNECTION|移除连接
3|ADD_CONSUMER|添加消费者
-3|ADD_CONSUMER_ACK|添加消费者回复
4|REMOVE_CONSUMER|移除消费者
5|ADD_PRODUCER|添加生产者
-5|REMOVE_PRODUCER_ACK|移除生产者回复
6|REMOVE_PRODUCER|移除生产者
7|HEARTBEAT|心跳
8|FETCH_HEALTH|拉取健康度
-8|FETCH_HEALTH_ACK|拉取健康度回复
10|FETCH_CLUSTER|拉取元数据
-10|FETCH_CLUSTER_ACK|拉取元数据回复
20|FIND_COORDINATOR|查找协调者
-20|FIND_COORDINATOR_ACK|查找协调者回复
21|FETCH_ASSIGNED_PARTITION|拉取分配分区
-21|FETCH_ASSIGNED_PARTITION|拉取分配分区回复
30|FETCH_TOPIC_MESSAGE|拉取主题消息
-30|FETCH_TOPIC_MESSAGE_ACK|拉取主题消息ACK
31|FETCH_PARTITION_MESSAGE|拉取分区消息
-31|FETCH_PARTITION_MESSAGE_ACK|拉取分区消息ACK
32|COMMIT_ACK|提交ACK
-32|COMMIT_ACK_ACK|提交ACK回复（保留）
33|COMMIT_ACK_INDEX|提交index
-33|COMMIT_ACK_INDEX_ACK|提交index回复
34|FETCH_ACK_INDEX|拉取ack index（保留）
-34|FETCH_ACK_INDEX_ACK|拉取ack index回复
35|FETCH_INDEX|拉取ack index
-35|FETCH_INDEX_ACK|拉取ack index回复
50|PRODUCE_MESSAGE|生产消息
-50|PRODUCE_MESSAGE_ACK|生产消息回复
51|PRODUCE_MESSAGE_PREPARE|预提交生产事务
-51|PRODUCE_MESSAGE_PREPARE_ACK|预提交生产事务回复
52|PRODUCE_MESSAGE_COMMIT|提交生产事务
-52|PRODUCE_MESSAGE_COMMIT_ACK|提交生产事务回复
53|PRODUCE_MESSAGE_ROLLBACK|回滚生产事务
-53|PRODUCE_MESSAGE_ROLLBACK_ACK|回滚生产事务回复
54|FETCH_PRODUCE_FEEDBACK|拉取事务补偿
-54|FETCH_PRODUCE_FEEDBACK_ACK|拉取事务补偿回复

## 错误码

code|描述
-|-
0|成功
1|无权限
2|认证失败
3|服务不可用
4|未知异常
5|数据库异常
6|参数错误
7|反对票
8|校验和出错
9|服务初始化出错
91|IO异常
92|消息序号超过最大值
93|消息序号小于最小值
101|磁盘刷新慢
107|序列化/反序列化错误
108|写入超时
109|写入错误
110|读取错误
111|复制入队超时
112|复制超时
113|复制异常
114|主从复制位置错误
115|刷新偏移量异常
116|同步状态不对
117|复制不能降级
131|连接已存在
132|连接不存在
134|生产者不存在
136|消费者不存在
137|事务已存在
138|事务不存在
139|提交事务失败
140|消费者ack失败
141|添加消息失败
142|获取消息失败
143|刷新顺序消息服务异常
144|该分组被主题设置为禁止发送
145|该分组被主题设置为禁止消费
146|该连接被应用者禁止发送
147|该连接被应用者禁止消费
148|该主题未提交的事务数量达到限制数
149|已经开启跨机房消费，本机房不能消费
150|选举异常
181|协调者不可用
182|协调者分配类型不存在
183|协调者分配错误
184|拉取消息index超出范围
185|协调者分配错误，没有可用分区
186|主题暂停消费
187|拉取失败，不是该主题leader
188|发送失败，不是该主题leader
189|TOPIC不存在
190|TOPIC无可用分组