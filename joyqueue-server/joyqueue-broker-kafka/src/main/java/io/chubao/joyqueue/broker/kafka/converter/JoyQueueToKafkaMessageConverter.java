package io.chubao.joyqueue.broker.kafka.converter;

import io.chubao.joyqueue.message.BrokerMessage;
import io.chubao.joyqueue.message.SourceType;
import io.chubao.joyqueue.network.serializer.BatchMessageSerializer;

import java.util.List;

/**
 * JoyQueueToKafkaMessageConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/3
 */
public class JoyQueueToKafkaMessageConverter extends AbstarctKafkaMessageConverter {

    @Override
    public BrokerMessage convert(BrokerMessage message) {
        message.setBody(message.getDecompressedBody());
        return message;
    }

    @Override
    public List<BrokerMessage> convertBatch(BrokerMessage message) {
        message.setBody(message.getDecompressedBody());
        return BatchMessageSerializer.deserialize(message);
    }

    @Override
    public Byte type() {
        return SourceType.JOYQUEUE.getValue();
    }
}