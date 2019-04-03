package com.jd.journalq.client.internal.consumer.converter.jmq2;

import com.google.common.collect.Lists;
import com.jd.journalq.client.internal.consumer.converter.MessageConverter;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.message.SourceType;

import java.util.List;

/**
 * Jmq2MessageConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/3
 */
public class Jmq2MessageConverter implements MessageConverter {

    @Override
    public BrokerMessage convert(BrokerMessage message) {
        return message;
    }

    @Override
    public List<BrokerMessage> convertBatch(BrokerMessage message) {
        return Lists.newArrayList(message);
    }

    @Override
    public Byte type() {
        return SourceType.JMQ2.getValue();
    }
}