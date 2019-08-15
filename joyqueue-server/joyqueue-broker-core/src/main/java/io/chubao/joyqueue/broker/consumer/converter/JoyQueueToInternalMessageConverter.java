package io.chubao.joyqueue.broker.consumer.converter;

import io.chubao.joyqueue.message.BrokerMessage;
import io.chubao.joyqueue.message.Message;
import io.chubao.joyqueue.message.SourceType;
import io.chubao.joyqueue.network.serializer.BatchMessageSerializer;

import java.util.List;

/**
 * JoyQueueToInternalMessageConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/24
 */
public class JoyQueueToInternalMessageConverter extends AbstractInternalMessageConverter {

    @Override
    public BrokerMessage convert(BrokerMessage message) {
        if (!message.isCompressed() || Message.CompressionType.ZLIB.equals(message.getCompressionType())) {
            return message;
        }
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