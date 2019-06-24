

# 快速启动

需要安装 Maven 和 Java 环境来启动 JournalQ

Maven 版本需要3.2或者更新的版本，JDK 8及以上版本


## 第1步：下载安装包或源码编译

```bash
$ git clone git@git.jd.com:next-generation-message-platform/JournalQ.git
$ cd JournalQ
$ mvn install -DskipTests
```

## 第2步：以默认配置启动JournalQ

```bash
$ cd distribution/journalq-server/bin
$ ./start.sh
```

现在可以通过启动日志和网络端口监听情况，判断JournalQ 和 命名（Naming）服务是否正常启动。
如果你能在日志中看到如下的日志，表明broker/选举/监控/命名服务已经正常启动，并监听在50088/50089/50090/50091端口上。

```
[11:06:21:241] [main] [INFO] - com.jd.journalq.broker.manage.exporter.BrokerManageExportServer.doStart(BrokerManageExportServer.java:57) - broker manage server is started, host: 10.0.17.78, port: 50090
[11:06:21:242] [main] [INFO] - com.jd.journalq.broker.BrokerService.doStart(BrokerService.java:263) - brokerServer start ,broker.id[1553137579],ip[10.0.17.78],frontPort[50088],backendPort[50089],monitorPort[50090],nameServer port[50091]
[11:06:21:243] [main] [INFO] - com.jd.journalq.broker.JournalqLauncher.main(JournalqLauncher.java:32) - >>>
>>>       _   __  __    ____  
>>>      | | |  \/  |  / __ \
>>>      | | | \  / | | |  | |
>>>  _   | | | |\/| | | |  | |
>>> | |__| | | |  | | | |__| |
>>> \______/ |_|  |_| \__\__\/
>>>                           
[11:06:21:243] [main] [INFO] - com.jd.journalq.broker.JournalqLauncher.main(JournalqLauncher.java:41) - JournalqLauncher is started
```

## 第3步：以默认配置启动JournalQ管理端

```bash
$ cd distribution/journalq-console/bin
$ ./start.sh
```

管理端元数据依托命名服务，默认连接本地命名服务。如有调整，请修改journalq-console/conf/important.properties中nameserver.host参数配置。
现在可以通过启动日志和网络端口监听情况，判断管理端服务是否正常启动。如果你能在日志中看到如下日志，表明管理端服务已经正常启动，并监听在10030端口上。

```
12:00:00.000 [routing-5] INFO  com.jd.laf.web.vertx.RoutingVerticle - success starting routing verticle 1 at 3f3a1221-6f00-44e5-b495-68048ed9909b
12:00:00.000 [routing-3] INFO  com.jd.laf.web.vertx.RoutingVerticle - success starting routing verticle 2 at 3f3a1221-6f00-44e5-b495-68048ed9909b
12:00:00.000 [routing-4] INFO  com.jd.laf.web.vertx.RoutingVerticle - success starting routing verticle 6 at 3f3a1221-6f00-44e5-b495-68048ed9909b
12:00:00.000 [routing-2] INFO  com.jd.laf.web.vertx.RoutingVerticle - success starting routing verticle 5 at 3f3a1221-6f00-44e5-b495-68048ed9909b
12:00:00.000 [routing-1] INFO  com.jd.laf.web.vertx.RoutingVerticle - success starting routing verticle 3 at 3f3a1221-6f00-44e5-b495-68048ed9909b
12:00:00.001 [routing-0] INFO  com.jd.laf.web.vertx.RoutingVerticle - success starting routing verticle 4 at 3f3a1221-6f00-44e5-b495-68048ed9909b
12:00:00.010 [vert.x-eventloop-thread-1] INFO  com.jd.laf.web.vertx.spring.SpringVertx - success deploying verticle com.jd.laf.web.vertx.spring.VerticleMeta@2616eb6a with deployment id 3f3a1221-6f00-44e5-b495-68048ed9909b
12:00:00.011 [main] INFO  com.jd.laf.web.vertx.spring.SpringVertx - success starting Vert.x
12:00:00.021 [main] INFO  com.jd.journalq.application.WebApplication - Started WebApplication in 20.462 seconds (JVM running for 22.553)
12:00:00.030 [routing-6] INFO  com.jd.laf.web.vertx.RoutingVerticle - success binding http listener 4 on port 10030
12:00:00.031 [routing-9] INFO  com.jd.laf.web.vertx.RoutingVerticle - success binding http listener 1 on port 10030
12:00:00.032 [routing-8] INFO  com.jd.laf.web.vertx.RoutingVerticle - success binding http listener 2 on port 10030
12:00:00.033 [routing-10] INFO  com.jd.laf.web.vertx.RoutingVerticle - success binding http listener 3 on port 10030
12:00:00.033 [routing-7] INFO  com.jd.laf.web.vertx.RoutingVerticle - success binding http listener 5 on port 10030
12:00:00.034 [routing-11] INFO  com.jd.laf.web.vertx.RoutingVerticle - success binding http listener 6 on port 10030
```
服务启动后，可以通过浏览器访问管理端，默认访问地址为: http://localhost:5000

## 第4步：创建发布和订阅关系
在开始生产和消费之前，需要先在管理端创建主题、生产和消费者应用以及对应的令牌，令牌用于认证应用的合法性。假设创建主题test_topic, 生产和消费应用都是同一个应用test_app，并且令牌为test_token。

## 第5步：生产和消费示例

JournalQ 提供Java、Spring 以及Spring Boot三种客户端使用形式。

### 第5.1步：引入Maven 依赖

```
<dependency>
    <groupId>com.jd.journalq</groupId>
    <artifactId>journalq-client-core</artifactId>
    <version>4.0.3</version>
</dependency>
```

### 第5.2 生产示例

```java
public static void main(String[] args) {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(JMQBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(String.format("oms:journalq://test_app@%s:50088/UNKNOWN", IpUtil.getLocalIp()), keyValue);

        Producer producer = messagingAccessPoint.createProducer();
        producer.start();

        producer.addInterceptor(new ProducerInterceptor() {
            @Override
            public void preSend(Message message, Context attributes) {
                System.out.println(String.format("preSend, message: %s", JSON.toJSONString(message)));
            }

            @Override
            public void postSend(Message message, Context attributes) {
                System.out.println(String.format("postSend, message: %s", JSON.toJSONString(message)));
            }
        });

        Message message = producer.createMessage("test_topic", "body".getBytes());
        SendResult sendResult = producer.send(message);
        System.out.println(String.format("messageId: %s", sendResult.messageId()));
    }
```

### 第5.3步：消费示例

```java
public static void main(String[] args) throws Exception {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(JMQBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(String.format("oms:journalq://test_app@%s:50088/UNKNOWN", IpUtil.getLocalIp()), keyValue);

        Consumer consumer = messagingAccessPoint.createConsumer();
        consumer.start();

        consumer.addInterceptor(new ConsumerInterceptor() {
            @Override
            public void preReceive(Message message, Context context) {
                System.out.println(String.format("preReceive, message: %s", JSON.toJSONString(message)));
            }

            @Override
            public void postReceive(Message message, Context context) {
                System.out.println(String.format("postReceive, message: %s", JSON.toJSONString(message)));
            }
        });

        consumer.bindQueue("test_topic", new MessageListener() {
            @Override
            public void onReceived(Message message, Context context) {
                System.out.println(String.format("onReceived, message: %s", JSON.toJSONString(message)));
                context.ack();
            }
        });

        System.in.read();
    }
```

## 第6步：Spring 方式生产和消费示例

### 第6.1步：引入Maven 依赖

```
<dependency>
    <groupId>com.jd.journalq</groupId>
    <artifactId>journalq-client-core</artifactId>
    <version>4.0.3</version>
</dependency>
<dependency>
    <groupId>io.openmessaging</groupId>
    <artifactId>openmessaging-spring</artifactId>
    <version>0.0.1-BETA-SNAPSHOT</version>
</dependency>

```

### 第6.2步：Spring-JournalQ.xml 配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:oms="http://openmessaging.io/schema"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://openmessaging.io/schema
	    http://openmessaging.io/schema/oms.xsd">
    <oms:access-point url="oms:journalq://test_app@localhost:50088/UNKNOWN">
        <oms:attribute key="ACCOUNT_KEY" value="test_token"></oms:attribute>
    </oms:access-point>
    <oms:producer id="producer"></oms:producer>
    <oms:consumer queueName="test_topic" listener="com.jd.journalq.client.samples.spring.MessageListener"></oms:consumer>
</beans>
```


### 第6.3步：生产示例

```java
protected static final Logger logger = LoggerFactory.getLogger(SpringMain.class);

   public static void main(String[] args) {
       ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("Spring-JournalQ.xml");
       Producer producer = (Producer) applicationContext.getBean("producer1");

       for (int i = 0; i < 10; i++) {
           Message message = producer.createMessage("test_topic", "test".getBytes());
           SendResult sendResult = producer.send(message);
           logger.info("sendResult: {}", sendResult);
       }
}
```   

### 第6.4步：消费示例
 Spring-JournalQ.xml 中的MessageListener 需要像【第4.3步：消费示例】一样实现 onReceived 方法。



## 第7步：Spring-boot 生产和消费示例

### 第7.1步：引入Maven 依赖

```
<dependency>
    <groupId>com.jd.journalq</groupId>
    <artifactId>journalq-client-core</artifactId>
    <version>4.0.3</version>
</dependency>
<dependency>
   <groupId>io.openmessaging</groupId>
   <artifactId>openmessaging-spring-boot-starter</artifactId>
   <version>0.0.1-BETA-SNAPSHOT</version>
   <exclusions>
      <exclusion>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
      </exclusion>
      <exclusion>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-to-slf4j</artifactId>
      </exclusion>
  </exclusions>
</dependency>   
```

### 第7.2步：Spring-boot application.properties配置

```
spring.oms.enable=true
spring.oms.url=oms:journalq://test_app@10.37.129.2:50088/UNKNOWN
spring.oms.attributes[ACCOUNT_KEY]=test_token

spring.oms.consumer.enable=true
spring.oms.producer.enable=true
spring.oms.interceptor.enable=true

```
### 第7.3步：生产示例

```java
@SpringBootApplication
@ComponentScan("com.jd.journalq.client.samples.springboot")
public class SpringBootMain {

  protected static final Logger logger = LoggerFactory.getLogger(SpringBootMain.class);

  public static void main(String[] args) {
      ConfigurableApplicationContext applicationContext = SpringApplication.run(SpringBootMain.class);
      Producer producer = applicationContext.getBean(Producer.class);

      for (int i = 0; i < 5; i++) {
          for (int j = 0; j < 10; j++) {
              Message message = producer.createMessage("test_topic_" + i, "test_body".getBytes());
              SendResult sendResult = producer.send(message);
              logger.info("sendResult: {}", sendResult);
          }
      }
  }
}
```  

### 第7.4步：消费示例

 JounalQ 定义了OMSMessageListener 注解，用于标识一个消费者。


```java
package com.jd.journalq.client.samples.springboot;

@Component
public class MessageListener1 {

    @OMSMessageListener(queueNames = "test_topic_0")
    public void onReceived(Message message, MessageListener.Context context) {
        System.out.println(String.format("receive, message: %s", JSON.toJSONString(message)));
        context.ack();
    }

    @OMSMessageListener(queueNames = "test_topic_1")
    public void onReceived(List<Message> messages, BatchMessageListener.Context context) {
        for (Message message : messages) {
            System.out.println(String.format("receive, message: %s", JSON.toJSONString(message)));
        }
        context.ack();
    }
}
```
