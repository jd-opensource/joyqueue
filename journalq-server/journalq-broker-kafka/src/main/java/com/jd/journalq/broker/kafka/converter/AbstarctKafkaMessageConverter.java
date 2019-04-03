package com.jd.journalq.broker.kafka.converter;

import com.jd.journalq.broker.consumer.MessageConverter;
import com.jd.journalq.message.SourceType;

/**
 * AbstarctKafkaMessageConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/3
 */
public abstract class AbstarctKafkaMessageConverter implements MessageConverter {

    @Override
    public byte target() {
        return SourceType.KAFKA.getValue();
    }
}