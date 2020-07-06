# 配置

## JoyQueue Server 配置

JoyQueue Server的配置文件位于joyqueue-server/conf/joyqueue.properties。

配置项 | 默认值 | 说明
-- | -- | --
application.data.path | ${HOME}/joyqueue | JoyQueue数据目录
broker.frontend-server.transport.server.port | 50088 | JoyQueue Server与客户端通信的端口。JoyQueue Server 会启用**连续的5个端口**用于通信，默认为：50088 - 50092。如果修改客户端端口号，其它的端口会自动跟随修改。
 -- | broker.frontend-server.transport.server.port + 1（50089） | 内部端口，JoyQueue Server各节点之间通信的端口
 -- | broker.frontend-server.transport.server.port + 2（50090） | JoyQueue Server rest API 端口，
 -- | broker.frontend-server.transport.server.port + 3（50091） | JoyQueue Web 使用这个端口与JoyQueue Server通信。
 -- | broker.frontend-server.transport.server.port + 4（50092） | 内部端口，JoyQueue Server 元数据服务端口。
 -- | broker.frontend-server.transport.server.port + 5（50093） | 内部端口，JoyQueue Server 元数据推送端口。
 -- | broker.frontend-server.transport.server.port + 6（50094） | 内部端口，JoyQueue Server Journalkeeper端口。
 broker.opts.memory | -Xms2G -Xmx2G -server  -Xss256K -XX:SurvivorRatio=8 -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:CMSInitiatingOccupancyFraction=70 -XX:+CMSParallelRemarkEnabled -XX:SoftRefLRUPolicyMSPerMB=0 -XX:CMSMaxAbortablePrecleanTime=20 -XX:-OmitStackTraceInFastThrow -XX:MaxDirectMemorySize=2G | jvm 参数。
store.message.file.size | 128 MB | 消息文件大小
store.index.file.size | 512 KB | 索引文件大小
store.index.file.load | false | 读取消息文件时，如果没有命中缓存，是否在内存中加载整个文件。设置为true时，整个文件就会被缓存到内存中，后续连续读取这个文件时性能更好。但加载文件太大时，会导致消费时延抖动。如果内存不够大，频繁的发生换页，也会导致文件缓存页被反复卸载再加载，反而引起不必要的磁盘IO，拖慢读取性能。<br/>设置为false时，由操作系统控制的PageCache作为读缓存。
store.index.file.load | true | 读取索引文件时，是否在内存中加载整个文件。
store.preload.buffer.core.count | 3 | 在写入时，数据先写入文件对应的缓存页，然后异步刷盘到文件中。每次更换一个新文件时，都需要申请一块儿和文件大小一致的内存作为文件的缓存页。为了提升写入性能，系统维护一个缓存页池。系统预先申请一些缓存页放入池中，需要申请时直接从池中获取，释放的缓存页则被还回缓存页池中，避免频繁的申请和释放内存。store.preload.buffer.core.count预加载DirectBuffer的缓存页的核心数量，缓存页池尽量维持池中可用的缓存页不少于这个数量。
store.preload.buffer.max.count | 10 | 缓存页池中缓存页最大数量，超过这个数量的缓存页将被释放。
store.max.message.length | 4 MB | 消息的最大长度，包含消息头。超过这个长度的消息将被拒绝写入。
store.write.request.cache.size | 128 | 每个Partition Group最多缓存的写入请求数量。
store.write.timeout | 3000 ms | 存储写入超时时间。
store.flush.interval | 50 ms | 存储刷盘最大时间间隔。
store.flush.force | true | 在写完每个文件之后，是否调用fsync刷盘。开启后，可以避免服务器宕机掉电而导致数据文件损坏，但会降低写入性能。
store.max.dirty.size | 10 MB | 脏数据的最大长度，如果内存中未刷盘的脏数据长度超过这个值，将阻塞消息写入。
store.disk.full.ratio | 90 | 磁盘空间使用率上限，超过这个上限将拒绝写入，默认为90%。
print.metric.interval | 0 ms | 打印存储监控信息的时间间隔，默认为0， 不打印。
store.clean.strategy.class | GlobalStorageLimitCleaningStrategy | 存储清理策略。存储清理策略决定JoyQueue如何去删除旧数据。默认清理策略下，系统首先尝试删除所有过期的数据，如果磁盘占用率仍高于清理上限store.disk.usage.max，则继续删除未过期的数据，直到磁盘利用率降低到清理上限或者没有任何数据可以删除。
store.max.store.size | 10 GB | 每个Partition Group最多保留数据大小。
store.max.store.time | 7天 | 数据最长保留时间。
store.clean.keep.unconsumed | true | 是否保留未消费的数据。开启后，清理策略将不会自动删除已订阅未消费（也就是积压的）数据。
store.disk.usage.max | 80(80%) | 磁盘使用率上限，超过这个上限开始清理。
store.disk.usage.safe | 75(%75) | 磁盘使用率下限，每次清理都会尽量将磁盘使用率清理至下限以下。
store.force.restore | true | 系统启动时，如果Broker磁盘上存储的Partition Group与NameServer上的元数据不一致时的处理方式。true: 以NameServer上的元数据为准，强制恢复Broker上数据。false: 如果不一致抛出异常，停止恢复。
nameserver.nsr.name | server | NameServer的启动方式：<br/> server: 默认的启动方式，存储元数据。<br/> thin: 不存储元数据，远程去其它Server读写元数据。
nameservice.serverAddress | 127.0.0.1:50092 | thin模式时，需要连接其它Server获取元数据，在这里配置其它Server的地址。这里配置的Server中，NameServer的启动方式必须是server模式。支持配置多个地址，用英文逗号隔开。例如：192.168.1.1:50092,192.168.1.2:50092。
nameserver.ignite.discoverySpi.localPort | 48500| Ignite服务发现本地端口
nameserver.ignite.discoverySpi.localPortRange | 20 | Ignite服务发现本地端口范围
nameserver.ignite.discoverySpi.networkTimeout | 5000 ms | Ignite服务发现超时
nameserver.ignite.discoverySpi.ipFinder.address | 127.0.0.1 | Ignite本地服务发现地址范围，支持多个地址，例如：1.2.3.4,1.2.3.5:47500..47509
nameserver.ignite.communicationSpi.localPort | 48100 | Ignite使用的通信端口号

## JoyQueue Server 内存参数配置

内存相关参数在broker.opts.memory中配置，JoyQueue会尽可能的使用物理内存（堆外内存）作为数据文件的缓存，以提升消息的收发性能，所以即使Broker相对空闲，也会一直保持比较高的内存占用率。

Broker占用的内存包括堆内存和堆外内存二部分。其中，堆外内存占用主要包括：

* JoyQueue管理的缓存页（绝大部分）；
* Socket缓冲区（少量）；
* 其它堆外内存（非常少）；

可以通过jvm参数控制JoyQueue内存使用，这两部分内存都可以通过如下参数来控制：

jvm参数 | 建议值 | 说明
-- | -- | --
-Xms -Xmx | 分配给JoyQueue物理内存*30%，不要低于256MB | 堆内存的最大最小值，建议配置成一样。
-XX:MaxDirectMemorySize | 分配给JoyQueue物理内存 - 堆内存最大值 | 最大堆外内存大小。JoyQueue使用的堆外内存如果超过这个上限，会触发FullGC。建议这个值设置的尽可能大，然后用PreloadBufferPool.MaxMemory来控制实际占用的堆外内存大小。如果未设置-XX:MaxDirectMemorySize，取值为JVM参数-Xmx
-DPreloadBufferPool.MaxMemory | MaxDirectMemorySize * 90% | 可供缓存使用的最大堆外内存。<br/> 1. 如果PreloadBufferPool.MaxMemory设置为数值，直接使用设置值。<br/> 2. 如果PreloadBufferPool.MaxMemory设置为百分比，比如：90%，最大堆外内存 = 物理内存 * 90% - 最大堆内存（由JVM参数-Xmx配置）<br/> 3. 如果PreloadBufferPool.MaxMemory未设置或者设置了非法值，最大堆外内存 = MaxDirectMemorySize * 90%。<br/> 4. 如果设置的最大堆外内存值超过了MaxDirectMemorySize * 90%，则最大堆外内存为：MaxDirectMemorySize * 90%。
-DPreloadBufferPool.WritePageExtraWeightMs | 默认值（1分钟）| 系统在内存满需要换页时，正在写入的页在置换时有额外的权重，这个权重用时间Ms体现。默认是60秒。置换权重 = 上次访问时间戳 + 额外权重，优先从内存中驱逐权重小的页。例如：一个只读的页，上次访问时间戳是T，一个读写页，上次访问时间是T - 60秒，这两个页在置换时有同样的权重。<br/>当Broker上活动Broker数量较多时（可用的最大堆外内存 < 活动的PartitionGroup数量 * 消息文件大小），这个值设置越大，写入页优先级越高，相应的写入性能会更好，但读取性能变差。

## JoyQueue Web 配置

JoyQueue Web的配置文件位于joyqueue-web/conf/application.properties。

配置项 | 默认值 | 说明
-- | -- | --
vertx.http.port | 10031 | Web服务端口
joyqueue.servers | 127.0.0.1:50091 | 提供元数据服务的JoyQueue Server地址和端口。支持配置多个地址，用逗号分开，默认端口为50091。例如：192.168.1.1,192.168.1.2:8888
