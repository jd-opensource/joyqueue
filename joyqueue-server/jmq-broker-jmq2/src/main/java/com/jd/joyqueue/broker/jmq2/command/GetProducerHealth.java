package com.jd.joyqueue.broker.jmq2.command;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;

/**
 * 健康检查，要判断读写权限和生产者是否存在
 */
public class GetProducerHealth extends GetHealth {
    // 生产者ID
    private String producerId;

    public GetProducerHealth topic(String topic) {
        setTopic(topic);
        return this;
    }

    public GetProducerHealth app(String app) {
        setApp(app);
        return this;
    }

    public GetProducerHealth producerId(String producerId) {
        setProducerId(producerId);
        return this;
    }

    public GetProducerHealth dataCenter(byte dataCenter) {
        setDataCenter(dataCenter);
        return this;
    }

    public String getProducerId() {
        return producerId;
    }

    public void setProducerId(String producerId) {
        this.producerId = producerId;
    }

    @Override
    public int type() {
        return JMQ2CommandType.GET_PRODUCER_HEALTH.getCode();
    }
}