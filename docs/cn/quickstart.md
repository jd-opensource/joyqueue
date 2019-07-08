

# 快速启动

需要安装 Maven 和 Java 环境来启动 JoyQueue

* 64 Linux/Mac 操作系统最佳
* Maven 3.2及以上版本
* JDK 8及以上版本


## 第1步：下载安装包或源码编译并安装

下载[JoyQueue Server](http://storage.jd.com/jmq4/joyqueue-server-4.1.0-SNAPSHOT.tar.gz?Expires=1566171865&AccessKey=6baa071a4e099393e996950bafc339240598e819&Signature=q1A2XMFZCDW8e5eq2duKc3tPaxc%3D)和
[JoyQueue Web](http://storage.jd.com/jmq4/joyqueue-server-4.1.0-SNAPSHOT.tar.gz?Expires=1566171865&AccessKey=6baa071a4e099393e996950bafc339240598e819&Signature=q1A2XMFZCDW8e5eq2duKc3tPaxc%3D)最新的安装包，从源码编译如下


```bash

$ git clone git@git.jd.com:laf/journalQ.git
$ cd journalQ && git checkout 4.1.0-SNAPSHOT
$ mvn install -Dmaven.test.skip=true -P CompileFrontend install
$ src_home=$(pwd)
$ mkdir ~/joyqueue-server ~/joyqueue-web
$ tar -zxvf  $src_home/joyqueue-distribution/target/joyqueue-server-4.1.0-SNAPSHOT.tar.gz -C ~/joyqueue-server
$ unzip $src_home/joyqueue-console/joyqueue-web/joyqueue-web-application/target/joyqueue-web-application-4.1.0-SNAPSHOT-assembly.zip  -d ~/joyqueue-web

```

## 第2步：以默认配置启动JoyQueue Server 

使用如下命令启动消息服务实例：

```bash

$ cd ~/joyqueue-server
$ bin/server-start.sh

```

现在可以通过启动日志和网络端口监听情况，判断JoyQueue 和 命名（Naming）服务是否正常启动。
如果你能在日志中看到如下的日志，表明broker/选举/监控/命名服务已经正常启动，并监听在50088/50089/50090/50091端口上。

```
信息: success starting routing verticle 1 at null
[19:17:13:180] [main] [INFO] - com.jd.joyqueue.broker.BrokerService.printConfig(BrokerService.java:302) - broker start with configuration:
	application.data.path: /var/root/.joyqueue
[19:17:13:180] [main] [INFO] - com.jd.joyqueue.broker.BrokerService.printConfig(BrokerService.java:303) - broker.id[1562576766],ip[10.0.16.231],frontPort[50088],backendPort[50089],monitorPort[50090],nameServer port[50091]
      _              ___
     | | ___  _   _ / _ \ _   _  ___ _   _  ___
  _  | |/ _ \| | | | | | | | | |/ _ \ | | |/ _ \
 | |_| | (_) | |_| | |_| | |_| |  __/ |_| |  __/
  \___/ \___/ \__, |\__\_\\__,_|\___|\__,_|\___|
              |___/

 ver. 4.1.0-SNAPSHOT

[19:17:13:183] [main] [INFO] - com.jd.joyqueue.broker.Launcher.main(Launcher.java:35) - JoyQueue is started

```

## 第3步：以默认配置启动JoyQueue Web 

使用如下命令启动管理端：
```bash

$ cd ～/joyqueue-web/
$ bin/start.sh

```

管理端元数据依托命名服务，默认连接本地命名服务。如有调整，请修改conf/application.properties中nameserver.host参数配置。
现在可以通过启动日志和网络端口监听情况，判断管理端服务是否正常启动。如果你能在日志中看到如下日志，表明管理端服务已经正常启动，并监听在10031端口上。正常的启动日志如下：

```
2019-07-08 18:26:49.676 [main] INFO  com.jd.laf.web.vertx.spring.SpringVertx - success starting Vert.x
2019-07-08 18:26:49.700 [main] INFO  com.jd.joyqueue.application.WebApplication - Started WebApplication in 4.821 seconds (JVM running for 6.16)
2019-07-08 18:26:49.705 [routing-6] INFO  com.jd.laf.web.vertx.RoutingVerticle - success binding http listener 5 on port 10031
2019-07-08 18:26:49.705 [routing-7] INFO  com.jd.laf.web.vertx.RoutingVerticle - success binding http listener 2 on port 10031
2019-07-08 18:26:49.706 [routing-9] INFO  com.jd.laf.web.vertx.RoutingVerticle - success binding http listener 4 on port 10031
2019-07-08 18:26:49.706 [routing-10] INFO  com.jd.laf.web.vertx.RoutingVerticle - success binding http listener 1 on port 10031
2019-07-08 18:26:49.705 [routing-8] INFO  com.jd.laf.web.vertx.RoutingVerticle - success binding http listener 6 on port 10031
2019-07-08 18:26:49.706 [routing-11] INFO  com.jd.laf.web.vertx.RoutingVerticle - success binding http listener 3 on port 10031


```
服务启动后，可以通过浏览器访问管理端，默认访问地址为: http://localhost:10031

## 第4步：创建发布和订阅关系

在开始生产和消费之前，需要先在管理端创建主题、生产和消费者应用以及对应的令牌，令牌用于认证应用的合法性。
假设创建一个独立的命名空间，假设为test;主题为joy_topic;生产和消费应用都是同一个应用为joyqueue,消费分组为abc;并且为joyqueue创建令牌，用于鉴权。
现在可以访问管理端，http://localhost:10031，依次创建生产及消费订阅关系

* 命名空间：test,操作路径：系统管理-》命名空间管理-》新建Namespace
* 主题：joy_test,操作路径：主题中心-》添加主题-》选择Broker
* 应用：joyqueue,操作路径：我的应用-》新建应用，应用负责人默认admin 
* 发布主题： 主题中心列表-》joy_test-》生产者》订阅-》选择客户端类型:joyqueue-》订阅
* 订阅主题： 主题中心列表-》joy_test》消费者》订阅》填写消费分组：abc-》选择端类型类型:joyqueue-》订阅
* 应用token: 我的应用列表-》joyqueue-》令牌-》添加


## 第5步：生产和消费示例

到目前为止，我们已经创建好应用和主题，可以尝试使用console-consumer和console-producer来生产和消费消息，这一步会利用到前面创建的topic,app及app token等信息。 

### 第5.1步：使用命令行生产和消费

生产消息，可以先利用*bin/console-producer.sh --help* 熟悉需要提供哪些参数。

```

 cd ~/joyqueue-server
 bin/console-producer.sh --help 
 bin/consol-producer.sh -a joyqueue --token bed6af17e9fd4ae3a767406446afe73f -t test.joy_topic -b "hello,jouqueue!"
   
```

消费消息,可以先利用*bin/console-consumer.sh --help* 熟悉需要提供哪些参数。

```

 cd ~/joyqueue-server
 bin/console-consumer.sh
 bin/console-consumer.sh -a joyqueue.abc --token bed6af17e9fd4ae3a767406446afe73f  -t test.joy_topic 
   
```


