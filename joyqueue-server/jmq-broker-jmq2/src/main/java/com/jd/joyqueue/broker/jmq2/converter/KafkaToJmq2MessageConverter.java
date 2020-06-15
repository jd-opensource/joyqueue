package com.jd.joyqueue.broker.jmq2.converter;

import org.joyqueue.broker.consumer.converter.KafkaToInternalMessageConverter;
import org.joyqueue.message.SourceType;

/**
 * KafkaToJmq2MessageConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/24
 */
public class KafkaToJmq2MessageConverter extends KafkaToInternalMessageConverter {

    @Override
    public byte target() {
        return SourceType.JMQ.getValue();
    }
}