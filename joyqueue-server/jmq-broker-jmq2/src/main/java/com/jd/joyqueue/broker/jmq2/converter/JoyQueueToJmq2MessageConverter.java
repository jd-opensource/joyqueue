package com.jd.joyqueue.broker.jmq2.converter;

import org.joyqueue.broker.consumer.converter.JoyQueueToInternalMessageConverter;
import org.joyqueue.message.SourceType;

/**
 * JoyQueueToJmq2MessageConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/24
 */
public class JoyQueueToJmq2MessageConverter extends JoyQueueToInternalMessageConverter {

    @Override
    public byte target() {
        return SourceType.JMQ.getValue();
    }
}