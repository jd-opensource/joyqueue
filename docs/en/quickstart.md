<!-- TOC START min:1 max:3 link:true asterisk:false update:true -->
- [Getting Started](#getting-started)
	- [Step 1: Download installment package or build from source](#step-1-download-installment-package-or-build-from-source)
	- [Step 2: Launch JournalQ server with default config](#step-2-launch-journalq-server-with-default-config)
	- [Step 3: Create topic and pub/sub relationship](#step-3-create-topic-and-pubsub-relationship)
	- [Step 4: Produce and consume example](#step-4-produce-and-consume-example)
		- [Step 4.1: Import maven dependency](#step-41-import-maven-dependency)
		- [Step 4.2: Produce example](#step-42-produce-example)
		- [Step 4.3: Consume example](#step-43-consume-example)
	- [Step 5: Produce and consume using Spring](#step-5-produce-and-consume-using-spring)
		- [Step 5.1: import maven dependency](#step-51-import-maven-dependency)
		- [Step 5.2: Spring-JournalQ.xml](#step-52-spring-journalqxml)
		- [Step 5.3: Produce example](#step-53-produce-example)
		- [Step 5.4: Consume example](#step-54-consume-example)
	- [Step 6: Produce and consume using Spring boot](#step-6-produce-and-consume-using-spring-boot)
		- [Step 6.1: Import maven dependency](#step-61-import-maven-dependency)
		- [Step 6.2: Spring boot application.properties](#step-62-spring-boot-applicationproperties)
		- [Step 6.3: Produce example](#step-63-produce-example)
		- [Step 6.4: Consume example](#step-64-consume-example)




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
$ cd distribution/jmq-server/bin
$ ./start.sh
```
Now,we will check whether JorunalQ and Naming Server started normally or not. Both startup logs and **netstat** port listen state can be used to validate.

If you see logs as following,which indicate broker/election/monitoring/naming/ service started and listening on 50088/50089/50090/50091 port:
```
[11:06:21:241] [main] [INFO] - com.jd.jmq.broker.manage.exporter.BrokerManageExportServer.doStart(BrokerManageExportServer.java:57) - broker manage server is started, host: 10.0.17.78, port: 50090
[11:06:21:242] [main] [INFO] - com.jd.jmq.broker.BrokerService.doStart(BrokerService.java:263) - brokerServer start ,broker.id[1553137579],ip[10.0.17.78],frontPort[50088],backendPort[50089],monitorPort[50090],nameServer port[50091]
[11:06:21:243] [main] [INFO] - com.jd.jmq.broker.JMQLauncher.main(JMQLauncher.java:32) - >>>
>>>       _   __  __    ____  
>>>      | | |  \/  |  / __ \
>>>      | | | \  / | | |  | |
>>>  _   | | | |\/| | | |  | |
>>> | |__| | | |  | | | |__| |
>>> \______/ |_|  |_| \__\__\/
>>>                           
[11:06:21:243] [main] [INFO] - com.jd.jmq.broker.JMQLauncher.main(JMQLauncher.java:41) - JMQLauncher is started
```

## Step 3: Create topic and pub/sub relationship

Before you can produce/consume messages, you should build up publish and subscribe relationship between application and topic. Assuming you have a topic named ***test_topic*** and producer/consume have the same name ***test_app***. And JounalQ provide a simple way,generating a token for each application,to check whether your app is legal or not. Assuming test_app has a token ***test_token***.

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

### Step 3.1 create topic

## Step 4: Produce and consume example

 In this section,You will try produce and consume message via JournalQ. There are three ways to access JournalQ,such as plain java,Spring or Spring boot style.

### Step 4.1: Import maven dependency

```
<dependency>
    <groupId>com.jd.jmq</groupId>
    <artifactId>jmq-client-core</artifactId>
    <version>4.0.0-SNAPSHOT</version>
</dependency>
```

### Step 4.2: Produce example

```java
public static void main(String[] args) {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(JMQBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(String.format("oms:jmq://test_app@%s:50088/UNKNOWN", IpUtil.getLocalIp()), keyValue);

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

### Step 4.3: Consume example

```java
public static void main(String[] args) throws Exception {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(JMQBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(String.format("oms:jmq://test_app@%s:50088/UNKNOWN", IpUtil.getLocalIp()), keyValue);

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

## Step 5: Produce and consume using Spring

### Step 5.1: import maven dependency

```
<dependency>
    <groupId>com.jd.jmq</groupId>
    <artifactId>jmq-client-core</artifactId>
    <version>4.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>io.openmessaging</groupId>
    <artifactId>openmessaging-spring</artifactId>
    <version>0.0.1-BETA-SNAPSHOT</version>
</dependency>

```
### Step 5.2: Spring-JournalQ.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:oms="http://openmessaging.io/schema"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://openmessaging.io/schema
	    http://openmessaging.io/schema/oms.xsd">
    <oms:access-point url="oms:jmq://test_app@localhost:50088/UNKNOWN">
        <oms:attribute key="ACCOUNT_KEY" value="test_token"></oms:attribute>
    </oms:access-point>
    <oms:producer id="producer1"></oms:producer>
    <oms:consumer queueName="test_topic" listener="com.jd.jmq.client.samples.spring.MessageListener1"></oms:consumer>
</beans>
```
### Step 5.3: Produce example

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
### Step 5.4: Consume example
The consumer which configured in Spring-JournalQ.xml should implement MessageListener as [Step 4.3: Consume example](#step 4.3 consume example).
Once the topic you have subscribe has new arriving messages,you consumer which configured in Spring-JournalQ.xml will be invoked.

## Step 6: Produce and consume using Spring boot

### Step 6.1: Import maven dependency
```
<dependency>
    <groupId>com.jd.jmq</groupId>
    <artifactId>jmq-client-core</artifactId>
    <version>4.0.0-SNAPSHOT</version>
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
### Step 6.2: Spring boot application.properties
```
spring.oms.enable=true
spring.oms.url=oms:jmq://test_app@10.37.129.2:50088/UNKNOWN
spring.oms.attributes[ACCOUNT_KEY]=test_token

spring.oms.consumer.enable=true
spring.oms.producer.enable=true
spring.oms.interceptor.enable=true

```
### Step 6.3: Produce example


```java
@SpringBootApplication
@ComponentScan("com.jd.jmq.client.samples.springboot")
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
### Step 6.4: Consume example


```java
package com.jd.jmq.client.samples.springboot;

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
