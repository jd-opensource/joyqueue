package org.joyqueue.broker.joyqueue0.converter;

import org.joyqueue.broker.consumer.converter.KafkaToInternalMessageConverter;
import org.joyqueue.message.SourceType;

/**
 * KafkaToJmq2MessageConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/24
 */
public class KafkaToJoyqueue0MessageConverter extends KafkaToInternalMessageConverter {

    @Override
    public byte target() {
        return SourceType.JOYQUEUE0.getValue();
    }
}