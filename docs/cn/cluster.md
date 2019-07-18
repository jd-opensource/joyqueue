# JoyQueue 集群部署

JoyQueue 的元数据依赖于Ignite服务，JoyQueue启动时会在本地启动一个Ignite client或server。
多个JoyQueue实例可以通过配置nameserver.ignite.discoverySpi.ipFinder.address在相同的ip范围或列举具体的ip，使它们组成元数据集群。
可以通过配置nameserver.ignite.clientMode指定Ignite节点的角色，默认为true。当clientMode=true时，本地Ignite作为Ignite server会保存元数据，否则不保存元数据。
Ignite的所有配置均可通过joyqueue.properties文件覆盖默认值。

JoyQueue的partition group是一个raft集群。在创建topic时，选择多个broker，则topic的partition groups将尽可能均匀的分布在broker上。
JoyQueue 依赖于Raft一致性协议来保障消息的可靠性，任意partition group都有多个副本，一般为三副本。

以三个节点的JoyQueue实例为例，假设ip分别为192.169.0.2,192.169.0.3,192.169.0.4。joyqueue.properties 文件包含如下配置：


```

nameserver.ignite.discoverySpi.ipFinder.address=192.169.0.2:48500..48520;,192.169.0.3:48500..48520;,192.169.0.4:48500..48520

```

分别启动这三个JoyQueue实例。新建主题joy_topic,在broker列表同时选择如上的三个ip，此时三副本的joy_topic就创建完成。






