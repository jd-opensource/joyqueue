package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;

/**
 * 获取消费位置应答解码器
 */
public class GetConsumeOffsetAckCodec extends GetConsumeOffsetCodec {

    @Override
    public int type() {
        return JMQ2CommandType.GET_CONSUMER_OFFSET_ACK.getCode();
    }
}