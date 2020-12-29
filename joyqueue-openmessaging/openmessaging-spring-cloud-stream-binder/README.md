# Spring Cloud Stream Binder for Openmessaging (JoyQueue)
## 关于（About Spring Cloud Stream） 
- 官方文档（Official document）: [docs.spring.io/spring-cloud-stream](https://docs.spring.io/spring-cloud-stream/docs/current/reference/html)

## 如何使用（How to use）
- 使用示例（Use sample） [Client Sample for Spring Cloud Stream](../../joyqueue-client/joyqueue-client-samples/joyqueue-client-samples-springcloud-stream)

## 配置（Configuration）
自定义消息处理接口
```java
@Component
public interface CustomProcessor {

    String INPUT_ORDER = "inputOrder";

    String OUTPUT_ORDER = "outputOrder";

    /**
     * 主题订阅通道
     *  <p>
     *     订阅消息通道{@link SubscribableChannel}为消息通道{@link MessageChannel}子类，该通道的所有消息被{@link org.springframework.messaging.MessageHandler}消息处理器所订阅
     *  </p>
     * @return {@link SubscribableChannel}
     */
    @Input(INPUT_ORDER)
    SubscribableChannel inputOrder();

    /**
     * 主题消息发布通道
     *
     * <p>
     *  消息通道{@link MessageChannel}用于接收消息，调用{@link MessageChannel#send(Message)}方法可以将消息发送至该消息通道中
     * </p>
     * @return {@link MessageChannel}
     */
    @Output(OUTPUT_ORDER)
    MessageChannel outputOrder();

}

```
Configuration file
```yaml
spring:
  cloud:
    stream:
      bindings:
        #对应CustomProcessor接口定义的outputOrder()
        outputOrder:
          binder: oms1
          destination: jqtopic
          content-type: text/plain
          producer:
            #通过该参数指定了分区键的表达式规则，可以根据实际的输出消息规则配置 SpEL 来生成合适的分区键
            partitionKeyExpression: payload
            partitionCount: 2
        inputOrder:
          binder: oms1
          destination: jqtopic
          content-type: text/plain
          group: group1
          consumer:
            concurrency: 50
      binders:
        oms1:
          type: oms
      default-binder: oms
      #OMS(JoyQueue)对应的连接配置
      oms:
        binder:
          url: oms:joyqueue://jqbinder@test-nameserver.jmq.xx.local:50088/UNKNOWN
          attributes:
            ACCOUNT_KEY: xxxx
        bindings:
          #OMS的自定义配置
          outputOrder:
            producer:
              group: demo-group
              sync: true
          inputOrder:
            consumer:
              enable: true
              batch: true
```