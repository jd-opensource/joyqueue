package com.jd.joyqueue.broker.jmq2.converter;

import org.joyqueue.broker.consumer.MessageConverter;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.message.SourceType;

import java.util.Arrays;
import java.util.List;

/**
 * Jmq2ToKafkaMessageConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/7/30
 */
public class Jmq2ToKafkaMessageConverter implements MessageConverter {

    public BrokerMessage convert(BrokerMessage message) {
        message.setBody(message.getDecompressedBody());
        return message;
    }

    public List<BrokerMessage> convertBatch(BrokerMessage message) {
        return Arrays.asList(message);
    }

    @Override
    public Byte type() {
        return SourceType.JMQ.getValue();
    }

    @Override
    public byte target() {
        return SourceType.KAFKA.getValue();
    }
}