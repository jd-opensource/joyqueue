<!-- TOC START min:1 max:3 link:true asterisk:false update:true -->
- [Getting Started](#getting-started)
	- [Step 1: Download installment package or build from source](#step-1-download-installment-package-or-build-from-source)
	- [Step 2: Launch JournalQ server with default config](#step-2-launch-journalq-server-with-default-config)
	- [Step 3: Launch JournalQ console with default config](#step-3-launch-journalq-console-with-default-config)
	- [Step 4: Create topic and pub/sub relationship](#step-3-create-topic-and-pubsub-relationship)
	- [Step 5: Produce and consume example](#step-4-produce-and-consume-example)
		- [Step 5.1: Import maven dependency](#step-41-import-maven-dependency)
		- [Step 5.2: Produce example](#step-42-produce-example)
		- [Step 5.3: Consume example](#step-43-consume-example)
	- [Step 6: Produce and consume using Spring](#step-5-produce-and-consume-using-spring)
		- [Step 6.1: import maven dependency](#step-51-import-maven-dependency)
		- [Step 6.2: Spring-JournalQ.xml](#step-52-spring-journalqxml)
		- [Step 6.3: Produce example](#step-53-produce-example)
		- [Step 6.4: Consume example](#step-54-consume-example)
	- [Step 7: Produce and consume using Spring boot](#step-6-produce-and-consume-using-spring-boot)
		- [Step 7.1: Import maven dependency](#step-61-import-maven-dependency)
		- [Step 7.2: Spring boot application.properties](#step-62-spring-boot-applicationproperties)
		- [Step 7.3: Produce example](#step-63-produce-example)
		- [Step 7.4: Consume example](#step-64-consume-example)




# Getting Started
You need to have Maven and Java installed.

JournalQ requires Maven 3.0 or higher.

Java 8 should be used in order to support lambda expression.

## Step 1: Download installment package or build from source

```bash
$ git clone git@git.jd.com:next-generation-message-platform/JournalQ.git
$ cd JournalQ
$ mvn install -DskipTests
```

## Step 2: Launch JournalQ server with default config

```bash
$ cd distribution/journalq-server/bin
$ ./start.sh
```
Now,we will check whether JorunalQ and Naming Server started normally or not. Both startup logs and **netstat** port listen state can be used to validate.

If you see logs as following,which indicate broker/election/monitoring/naming/ server started and listening on 50088/50089/50090/50091 port:
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

## Step 3: Launch JournalQ console with default config

```bash
$ cd distribution/journalq-console/bin
$ ./start.sh
```

Console Server depends on Naming server. It will start with the default configuration without any modification. 
But if Naming server did not start at the same server, you need modify the 'nameserver.host' parameter configuration of the 'important.properties' property file under the 'journalq-console/conf' directory.

Now, we can check whether Console Server started normally or not. Both startup logs and **netstat** port listen state can be used to validate.
If you see logs as following, which indicate the console service started and listening on 10030 port:

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

After server started normally, we can access the console through the browser. The url is http://localhost:5000.

## Step 4: Create topic and pub/sub relationship

Before you can produce/consume messages, you should build up publish and subscribe relationship between application and topic through the console. Assuming you have a topic named ***test_topic*** and producer/consume have the same name ***test_app***. And JounalQ provide a simple way,generating a token for each application,to check whether your app is legal or not. Assuming test_app has a token ***test_token***.

Making sure JournalQ have started, **topic.sh** will assist you to config.

* Create topic,in terminal:

```
$ ./topic.sh  -c create -b 1553137579 -t test_topic
success
```
* Publish to test_topic:

```
$ ./topic.sh  -c publish -t test_topic -a test_app
success

```
* Subscribe to test_topic:

```
$ ./topic.sh  -c subscribe -t test_topic -a test_app
success
```
* Generate a token for test_app application:

```
$ ./topic.sh  -c token  -a test_app
use default token:test_token
success
```

### Step 4.1 create topic

## Step 5: Produce and consume example

 In this section,You will try produce and consume message via JournalQ. There are three ways to access JournalQ,such as plain java,Spring or Spring boot style.

### Step 5.1: Import maven dependency

```
<dependency>
    <groupId>com.jd.journalq</groupId>
    <artifactId>journalq-client-core</artifactId>
    <version>4.0.3</version>
</dependency>
```

### Step 5.2: Produce example

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

### Step 5.3: Consume example

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

## Step 6: Produce and consume using Spring

### Step 6.1: import maven dependency

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
### Step 6.2: Spring-JournalQ.xml

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
    <oms:producer id="producer1"></oms:producer>
    <oms:consumer queueName="test_topic" listener="com.jd.journalq.client.samples.spring.MessageListener1"></oms:consumer>
</beans>
```
### Step 6.3: Produce example

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
### Step 6.4: Consume example
The consumer which configured in Spring-JournalQ.xml should implement MessageListener as [Step 4.3: Consume example](#step 4.3 consume example).
Once the topic you have subscribe has new arriving messages,you consumer which configured in Spring-JournalQ.xml will be invoked.

## Step 7: Produce and consume using Spring boot

### Step 7.1: Import maven dependency
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
### Step 7.2: Spring boot application.properties
```
spring.oms.enable=true
spring.oms.url=oms:journalq://test_app@10.37.129.2:50088/UNKNOWN
spring.oms.attributes[ACCOUNT_KEY]=test_token

spring.oms.consumer.enable=true
spring.oms.producer.enable=true
spring.oms.interceptor.enable=true

```
### Step 7.3: Produce example


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
### Step 7.4: Consume example


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
