package io.chubao.joyqueue.broker.kafka.converter;

import io.chubao.joyqueue.broker.consumer.MessageConverter;
import io.chubao.joyqueue.message.SourceType;

/**
 * AbstarctKafkaMessageConverter
 *
 * author: gaohaoxiang
 * date: 2019/4/3
 */
public abstract class AbstarctKafkaMessageConverter implements MessageConverter {

    @Override
    public byte target() {
        return SourceType.KAFKA.getValue();
    }
}