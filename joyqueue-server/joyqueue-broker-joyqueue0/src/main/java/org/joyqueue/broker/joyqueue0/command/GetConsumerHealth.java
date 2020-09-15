package org.joyqueue.broker.joyqueue0.command;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;

/**
 * 健康检查，要判断读写权限和消费者是否存在
 */
public class GetConsumerHealth extends GetHealth {
    // 消费者ID
    private String consumerId;

    public GetConsumerHealth topic(String topic) {
        setTopic(topic);
        return this;
    }

    public GetConsumerHealth app(String app) {
        setApp(app);
        return this;
    }

    public GetConsumerHealth consumerId(String consumerId) {
        setConsumerId(consumerId);
        return this;
    }

    public GetConsumerHealth dataCenter(byte dataCenter) {
        setDataCenter(dataCenter);
        return this;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    @Override
    public int type() {
        return Joyqueue0CommandType.GET_CONSUMER_HEALTH.getCode();
    }
}