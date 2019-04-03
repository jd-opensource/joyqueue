package com.jd.journalq.broker.kafka.converter;

import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.message.SourceType;
import com.jd.journalq.network.serializer.BatchMessageSerializer;

import java.util.List;

/**
 * JournalqToKafkaMessageConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/3
 */
public class JournalqToKafkaMessageConverter extends AbstarctKafkaMessageConverter {

    @Override
    public BrokerMessage convert(BrokerMessage message) {
        return message;
    }

    @Override
    public List<BrokerMessage> convertBatch(BrokerMessage message) {
        message.setBody(message.getDecompressedBody());
        return BatchMessageSerializer.deserialize(message);
    }

    @Override
    public Byte type() {
        return SourceType.KAFKA.JMQ.getValue();
    }
}