

# 快速开始

需要安装Java 8及以上版本来启动 JoyQueue

确认Java环境以及Java版本正确：

```bash
$ java -version
java version "1.8.0_202"
Java(TM) SE Runtime Environment (build 1.8.0_202-b08)
Java HotSpot(TM) 64-Bit Server VM (build 25.202-b08, mixed mode)
```

## 第1步：下载安装包

在[这里（**TODO：需要替换成我们的下载页地址**）](https://github.com/shadowsocks/shadowsocks-windows/releases/latest)下载JoyQueue Server和JoyQueue Web的最新版本。

## 第2步：启动JoyQueue Server

使用`server-start.sh`命令启动消息服务实例:

```bash
$ joyqueue-server-4.1.0-SNAPSHOT/bin/server-start.sh

......

[19:17:13:180] [main] [INFO] - com.jd.joyqueue.broker.BrokerService.printConfig(BrokerService.java:303) - broker.id[1562576766],ip[10.0.16.231],frontPort[50088],backendPort[50089],monitorPort[50090],nameServer port[50091]
      _              ___
     | | ___  _   _ / _ \ _   _  ___ _   _  ___
  _  | |/ _ \| | | | | | | | | |/ _ \ | | |/ _ \
 | |_| | (_) | |_| | |_| | |_| |  __/ |_| |  __/
  \___/ \___/ \__, |\__\_\\__,_|\___|\__,_|\___|
              |___/

 ver. 4.1.0-SNAPSHOT

[19:17:13:183] [main] [INFO] - com.jd.joyqueue.broker.Launcher.main(Launcher.java:35) - JoyQueue is started
......
```

控制台上打印JoyQueue的LOGO说明JoyQueue Server启动成功。

## 第3步：启动JoyQueue Web

JoyQueue Web，默认连接本机的JoyQueue Server。如果JoyQueue Web与JoyQueue Server部署在不同的服务器上，需要修改`conf/application.properties`中`nameserver.host`参数配置。

使用`start.sh`启动管理端:

```bash

$ joyqueue-web-4.1.0-SNAPSHOT/bin/start.sh

......
2019-07-09 13:04:25.319 [main] INFO  com.jd.laf.web.vertx.spring.SpringVertx - success starting Vert.x
2019-07-09 13:04:25.329 [main] INFO  com.jd.joyqueue.application.WebApplication - Started WebApplication in 4.179 seconds (JVM running for 4.776)
2019-07-09 13:04:25.331 [main] INFO  com.jd.joyqueue.application.WebApplication - JoyQueue web started on port 10031.

......
```

在控制台观察到如下日志，表明JoyQueue Web已经正常启动，并监听在10031端口:

> JoyQueue web started on port 10031.

服务启动后，可以通过浏览器访问管理端，默认访问地址为: [http://localhost:10031](http://localhost:10031)。

## 第4步：创建主题和发布订阅关系

在开始生产和消费之前，需要先在管理端创建主题、生产者和消费者应用以及对应的令牌，令牌用于认证应用的合法性。

* 主题：joy_topic
* 应用：joyqueue
现在可以访问管理端: [http://localhost:10031](http://localhost:10031)

依次创建生产及消费订阅关系：

* 创建主题：joy_topic，操作路径：主题中心 - 添加主题
* 创建应用：joyqueue，操作路径：我的应用 - 新建应用
* 创建令牌：操作路径：我的应用 - joyqueue - 详情 - 令牌 - 添加
* 订阅生产：操作路径：主题中心 - joy_topic - 生产者 - 点击“订阅”按钮 - 找到应用代码为“joyqueue"的行 - ”选择客户端类型:joyqueue - 点击行尾“订阅”按钮
* 订阅消费：操作路径：主题中心 - joy_topic - 消费者 - 点击“订阅”按钮 - 找到应用代码为“joyqueue"的行 - ”选择客户端类型:joyqueue - 点击行尾“订阅”按钮

## 第5步：发送和接收消息

到目前为止，我们已经创建好主题和应用及其token，并且已经维护好发布/订阅关系。可以尝试生产和消费消息，这一步会利用到前面创建的topic，app及app token等信息。

### 第5.1步：发送消息

使用`console-producer.sh`来发送消息，**注意其中的token要替换成上一步在管理端创建的令牌**：

```bash

 $ joyqueue-server-4.1.0-SNAPSHOT/bin/console-producer.sh -a joyqueue --token a768388469e144b0b6cbe87a6e339a3c -t joy_topic -b "Hello,JoyQueue"
   
```

### 第5.2步：使用脚本消费消息

使用`console-consumer.sh`来接收消息：

```bash
$ joyqueue-server-4.1.0-SNAPSHOT/bin/console-consumer.sh -a joyqueue --token a768388469e144b0b6cbe87a6e339a3c -t joy_topic

Message{topic: joy_topic, partition: 0, index: 0, txId: null, key: null, body: Hello,JoyQueue}
```

## 第6步： 使用Docker镜像体验JoyQueue

* 启动JoyQueue server 和 web 服务

JoyQueue server 镜像(joyqueue/joyqueue-server)默认会启动一个本地管理端

```bash

$ docker run -p 80:10031  -d  --name joy joyqueue/joyqueue-server

```

现在可以访问管理端: [http://localhost:10031](http://localhost:10031)

* 参考[第4步：创建发布和订阅关系](##第4步：创建发布和订阅关系)

* 发送和接受消息

```bash
 $ docker exec -it  joy  bin/console-producer.sh -a joyqueue --token a768388469e144b0b6cbe87a6e339a3c -t joy_topic -b "Hello,JoyQueue"
 $ docker exec -it  joy  bin/console-consumer.sh -a joyqueue --token a768388469e144b0b6cbe87a6e339a3c -t joy_topic

```
