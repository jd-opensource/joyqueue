# 客户端使用说明

JoyQueue 支持Openmessaging协议，并提供Java版本的原生客户端。此外，JoyQueue Server支持Kafka和MQTT的客户端，你也可以使用Kafka的客户端和各种支持MQTT协议的客户端配合JoyQueue Server来使用。

下文将介绍JoyQueue客户端在纯Java、Spring及Sping boot 环境的基本使用示例，以及Kafka、MQTT客户端的使用。

[查看示例代码](../../joyqueue-client/joyqueue-client-samples)。

## OpenMessaging Java客户端

使用Maven构建，在pom.xml中加入JoyQueue客户端引用：

```xml
<dependency>
    <groupId>org.joyqueue</groupId>
    <artifactId>joyqueue-client-all</artifactId>
    <version>4.1.1</version>
</dependency>
```

### Producer

````java
public class SimpleProducer {

    public static void main(String[] args) {
        final String app = "test_app";
        final String token = "some token";
        final String dataCenter = "default";
        final String brokerAddr = "127.0.0.1:50088";
        final String topic = "test_topic_0";
        // oms:joyqueue://test_app@127.0.0.1:50088/default
        final String url = "oms:joyqueue://" + app +  "@" + brokerAddr + "/" + dataCenter;

        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, token);

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(url, keyValue);

        // 使用MessagingAccessPoint创建producer
        Producer producer = messagingAccessPoint.createProducer();
        producer.start();

        // 使用producer.createMessage方法创建message
        Message message = producer.createMessage(topic, "Message body".getBytes());

        // 生产消息，不抛异常就算成功，sendResult里的messageId暂时没有意义
        SendResult sendResult = producer.send(message);

        // 打印生产结果
        System.out.println(String.format("messageId: %s", sendResult.messageId()));

    }
}
````

### Consumer

````java
public class SimpleConsumer {

    public static void main(String[] args) throws Exception {
        final String app = "test_app";
        final String token = "some token";
        final String dataCenter = "default";
        final String brokerAddr = "127.0.0.1:50088";
        final String topic = "test_topic_0";
        // oms:joyqueue://test_app@127.0.0.1:50088/default
        final String url = "oms:joyqueue://" + app +  "@" + brokerAddr + "/" + dataCenter;

        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, token);

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(url, keyValue);

        // 创建consumer实例
        Consumer consumer = messagingAccessPoint.createConsumer();

        // 绑定需要消费的topic和对应的listener
        consumer.bindQueue(topic, new MessageListener() {
            @Override
            public void onReceived(Message message, Context context) {
                System.out.println(String.format("onReceived, message: %s", message));

                // 确认消息消费成功，如果没有确认或抛出异常会进入重试队列
                context.ack();
            }
        });

        consumer.start();
        System.in.read();
    }
}
````

### 拦截器

JoyQueue支持在生产和消费时，定义消息拦截器，用于实现日志，监控，消息过滤等功能，JoyQueue支持OpenMessaging中定义的拦截器API，还额外提供消息过滤，拦截器排序等功能，注意Consumer拦截器只在使用listener时生效。

#### OpenMessaging Consumer拦截器

````java
public class SimpleConsumerInterceptor {
    
    private Consumer consumer;

    public static void main(String[] args) throws Exception {
        consumer.addInterceptor(new ConsumerInterceptor() {
            @Override
            public void preReceive(Message message, Context context) {
                // 消费前执行
                System.out.println(String.format("preReceive, message: %s", message));
            }

            @Override
            public void postReceive(Message message, Context context) {
                // 消费后执行
                System.out.println(String.format("postReceive, message: %s", message));
            }
        });

        consumer.bindQueue("test_topic_0", new MessageListener() {
            @Override
            public void onReceived(Message message, Context context) {
                System.out.println(String.format("onReceived, message: %s", message));
                context.ack();
            }
        });
    }
}
````

#### JoyQueue Consumer拦截器

拦截器使用时需要通过spi的方式注册，把实现类添加到META-INF/services/org.joyqueue.client.internal.consumer.interceptor.ConsumerInterceptor里。

````java
// Ordered接口提供getOrder方法，用于指定顺序，可以不实现
// context还有attributes等可使用，具体看org.joyqueue.client.internal.consumer.interceptor.ConsumeContext
public class JoyQueueSimpleConsumerInterceptor implements ConsumerInterceptor, Ordered {

    @Override
    public boolean preConsume(ConsumeContext context) {
        System.out.println("preConsume");
        
        // 循环一批消息，单条和批消息都是按批拦截
        for (ConsumeMessage message : context.getMessages()) {
            // 过滤消息
            context.filterMessage(message);
        }
        
        // 返回true表示这批消息可以消费，返回false表示这批消息不可消费
        return true;
    }

    @Override
    public void postConsume(ConsumeContext context, List<ConsumeReply> consumeReplies) {
        System.out.println("postConsume");
    }

    @Override
    public int getOrder() {
        // 值小的先执行
        return Ordered.LOWEST_PRECEDENCE;
    }
}
````

#### OpenMessaging Producer拦截器

````java
public class SimpleProducerInterceptor {
    
    private Producer producer;

    public static void main(String[] args) {
        producer.addInterceptor(new ProducerInterceptor() {
            @Override
            public void preSend(Message message, Context attributes) {
                // 发送前执行
                System.out.println(String.format("preSend, message: %s", message));
            }

            @Override
            public void postSend(Message message, Context attributes) {
                // 发送后执行
                System.out.println(String.format("postSend, message: %s", message));
            }
        });

        producer.start();

        Message message = producer.createMessage("test_topic_0", "body".getBytes());
        SendResult sendResult = producer.send(message);
        System.out.println(String.format("messageId: %s", sendResult.messageId()));
    }
}
````

#### JoyQueue Producer拦截器 

使用时需要通过spi的方式注册，把实现类添加到 META-INF/services/org.joyqueue.client.internal.producer.interceptor.ProducerInterceptor 里。

````java
// Ordered接口提供getOrder方法，用于指定顺序，可以不实现
// context还有attributes等可使用，具体看org.joyqueue.client.internal.producer.interceptor.ProduceContext
public class JoyQueueSimpleProducerInterceptor implements ProducerInterceptor, Ordered {

    @Override
    public boolean preSend(ProduceContext context) {
        System.out.println("preSend");

        // 循环一批消息，单条生产和批量生产都是按批拦截
        for (ProduceMessage message : context.getMessages()) {
        }

        // 返回true表示这批消息可以生产，返回false表示这批消息不可生产
        return true;
    }

    @Override
    public void postSend(ProduceContext context, List<SendResult> result) {
        System.out.println("postSend");
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
````

## 与Spring集成

使用Maven构建，在pom.xml中加入JoyQueue客户端引用：

```xml
<dependency>
    <groupId>org.joyqueue</groupId>
    <artifactId>joyqueue-client-all</artifactId>
    <version>4.1.0</version>
</dependency>
<dependency>
    <groupId>org.joyqueue</groupId>
    <artifactId>openmessaging-spring</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
配置spring-sample.xml
````xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:oms="http://openmessaging.io/schema"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://openmessaging.io/schema
        http://openmessaging.io/schema/oms.xsd">
    
    <!-- 定义accessPoint -->
    <oms:access-point url="oms:joyqueue://test_app@nameserver.joyqueue.local:50088/UNKNOWN">
        <oms:attribute key="ACCOUNT_KEY" value="test_token"></oms:attribute>
        <!-- 更多配置 -->
    </oms:access-point>

    <!-- 创建producer实例，由spring创建并管理生命周期，不需要再手动调用start -->
    <oms:producer id="producer1"></oms:producer>
    
    <!-- 创建一个consumer实例，由spring创建并管理生命周期，不需要再手动调用bind和start -->
    <!-- queue-name对应需要消费的主题 -->
    <!-- listener需要实现io.openmessaging.consumer.MessageListener或io.openmessaging.consumer.BatchMessageListener接口，对应单条和批量消费 -->
    <oms:consumer queue-name="test_topic_0" listener="org.joyqueue.client.samples.spring.SimpleMessageListener"></oms:consumer>
    
</beans>
````

初始化Spring Application Context，然后发送消息：

```java
public class SpringMain {

    protected static final Logger logger = LoggerFactory.getLogger(SpringMain.class);

    public static void main(String[] args) {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-sample.xml");
        Producer producer = (Producer) applicationContext.getBean("producer1");

        for (int i = 0; i < 10; i++) {
            Message message = producer.createMessage("test_topic_0", "test".getBytes());
            SendResult sendResult = producer.send(message);
            logger.info("Message ID: {}", sendResult.messageId());
        }
    }
}
```

用于接收消息的SimpleMessageListener：

```java
public class SimpleMessageListener implements MessageListener {

    @Override
    public void onReceived(Message message, Context context) {
        System.out.println(String.format("receive, message: %s", message));
    }
}
```


## 与Spring Boot集成

使用Maven构建，在pom.xml中加入JoyQueue客户端和OpenMessaging Spring Boot Starter引用：

```xml
<dependency>
    <groupId>org.joyqueue</groupId>
    <artifactId>joyqueue-client-all</artifactId>
    <version>4.1.0</version>
</dependency>
<dependency>
    <groupId>io.openmessaging</groupId>
    <artifactId>openmessaging-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
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

配置application.properties

````properties
#spring.oms.url是核心配置，必须配置，否则无法使用
#spring.oms.attributes是一些配置信息，里面的ACCOUNT_KEY必须配置
spring.oms.url=oms:joyqueue://test_app@nameserver.joyqueue.local:50088/UNKNOWN
spring.oms.attributes[ACCOUNT_KEY]=test_token

#是否启用消费者, 默认启用
#spring.oms.consumer.enable=false

#是否启用生产者，默认启用
#spring.oms.producer.enable=false

#是否启用拦截器，默认启用
#spring.oms.interceptor.enable=false

#是否启用事务补偿，默认启用
#spring.oms.producer.transaction.check.enable=false
````

### 发送消息

默认配置下SpringBoot会自动创建一个Producer bean，可直接使用：

````java
@SpringBootApplication
@ComponentScan("org.joyqueue.client.samples.springboot")
public class SpringBootMain implements InitializingBean {

    protected static final Logger logger = LoggerFactory.getLogger(SpringBootMain.class);

    // 注入producer实例
    @Resource
    private Producer producer;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootMain.class);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (int i = 0; i < 10; i++) {
            Message message = producer.createMessage("test_topic_0", "test".getBytes());
            SendResult sendResult = producer.send(message);
            logger.info("sendResult: {}", sendResult);
        }
    }
}
````

### 接收消息

Consumer需要使用io.openmessaging.spring.boot.annotation.OMSMessageListener注解定义，
分为两种使用方式，一种是通过类实现接口的方式，另一种是通过方法直接消费。

* 实现接口

````java
@Component // 注册到spring
@OMSMessageListener(queueName = "test_topic_0") // 声明类一个consumer，并消费test_topic_0
// 也可以实现io.openmessaging.consumer.BatchMessageListener批量消费
public class SimpleMessageListener implements MessageListener {

    @Override
    public void onReceived(Message message, MessageListener.Context context) {
        System.out.println(String.format("receive, message: %s", message));
        context.ack();
    }
}
````

* 方法消费

注意方法参数不能写错。

````java
@Component // 注册到spring
public class SimpleMessageListener {

    @OMSMessageListener(queueName = "test_topic_0") // 指定方法消费test_topic_0
    public void onReceived(Message message, MessageListener.Context context) {
        System.out.println(String.format("receive, message: %s", message));
        context.ack();
    }

    @OMSMessageListener(queueName = "test_topic_1") // 指定方法批量消费test_topic_1
    public void onReceived(List<Message> messages, BatchMessageListener.Context context) {
        for (Message message : messages) {
            System.out.println(String.format("receive, message: %s", message));
        }
        context.ack();
    }
}
````

### 拦截器

拦截器也是通过注解声明

````java
@OMSInterceptor // 声明是一个拦截器，已包含Component注解，可直接被spring扫描
// 可以实现ProducerInterceptor或ConsumerInterceptor接口
public class SimpleProducerInterceptor implements ProducerInterceptor {

    @Override
    public void preSend(Message message, Context attributes) {
        System.out.println(String.format("preSend, message: %s", message));
    }

    @Override
    public void postSend(Message message, Context attributes) {
        System.out.println(String.format("postSend, message: %s", message));
    }
}
````

## Kafka客户端

JoyQueue兼容kafka协议，可直接使用原生kafka客户端。  

使用Kafka客户端连接JoyQueue时，与连接Kafka server使用方式一样，唯一区别需要指定group.id和client.id为JoyQueue的app。 

**使用Kafka客户端接入JoyQueue Server时，不需要填写Token，JoyQueue Server也不会验证Token**。

### 使用kafka-java客户端



kafka客户端建议使用1.0.0版本以上，兼容0.9及以上版本，不兼容0.8及以下版本。

pom 依赖
````xml

<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>2.1.0</version>
</dependency>

````

#### 发送消息

````java
public class SimpleKafkaProducer {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:50088");
        
        // 需指定client.id，值是JoyQueue的app
        // 如果特殊情况需要多个producer实例，可以在app后加 '-' 区分，比如 test_app-0,test_app-1
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "test_app");
        
        // 其他配置根据实际情况配置，这里的只是演示
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(props);

        kafkaProducer.send(new ProducerRecord<String, String>("test_topic_0", "test"));
    }
}
````

#### 接收消息

````java
public class SimpleKafkaConsumer {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfigs.BOOTSTRAP);
        
        // 需指定group.id，值是管理端的app
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfigs.GROUP_ID);
        
        // 需指定client.id，值是管理端的app
        // 如果特殊情况需要多个producer实例，可以在app后加 '-' 区分，比如 test_app-0,test_app-1
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, KafkaConfigs.GROUP_ID);
        
        // 其他配置根据实际情况配置，这里只是演示
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("test_topic_0"));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000 * 1));
            for (ConsumerRecord<String, String> record : records) {
                System.out.println(String.format("record, key: %s, value: %s, offset: %s", record.key(), record.value(), record.offset()));
            }
        }
    }
}
````

### 使用Kafka-python客户端

git 地址：[https://github.com/dpkp/kafka-python](https://github.com/dpkp/kafka-python), 使用说明：

注意事项：

* 生产时如果有异常需要根据业务逻辑做异常处理，重新发送消息；
* 支持发送到指定key和partition；
* 压缩方式建议使用gzip。其它压缩方式中，lz4和snappy服务端支持，但客户端配置较复杂，zstd服务端不支持；
  
安装 kafka-python

```bash
$ pip install kafka-python
```

发送消息：

```python
    from kafka import KafkaProduce
    topic="your_topic"
    message="test_message"
    conf={
        'bootstrap_servers':'127.0.0.1:50088',
        'client_id':'your_app'
    }
    producer = KafkaProducer(**conf)
    try:
        future=producer.send(your_topic,message)
        result = future.get(timeout=60)
    except BaseException as e:
        # 发送失败时，用户需根据业务逻辑做异常处理，否则消息可能会丢失
        print(e)
```

接收消息：

```python
    from kafka import KafkaConsumer

    conf={
        'bootstrap_servers':'127.0.0.1:50088',
        'client_id':'your_app'
    }
    conf['group_id'] = 'your_app'
    consumer = KafkaConsumer(your_topic,**conf)
    for msg in consumer:
        print(msg)
```
