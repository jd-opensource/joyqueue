package com.jd.joyqueue.broker.jmq2.command;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;

/**
 * 合并消费位置应答
 */
public class GetConsumeOffsetAck extends GetConsumeOffset {
    @Override
    public int type() {
        return JMQ2CommandType.GET_CONSUMER_OFFSET_ACK.getCode();
    }
}
