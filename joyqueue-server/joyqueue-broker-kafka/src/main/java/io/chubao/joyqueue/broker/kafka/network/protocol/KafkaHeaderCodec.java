package io.chubao.joyqueue.broker.kafka.network.protocol;

import io.chubao.joyqueue.broker.kafka.network.KafkaHeader;
import io.chubao.joyqueue.domain.QosLevel;
import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.command.Direction;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhuduohui on 2018/9/2.
 */
public class KafkaHeaderCodec implements Codec {
    private static Logger logger = LoggerFactory.getLogger(KafkaHeaderCodec.class);

    @Override
    public KafkaHeader decode(ByteBuf buffer) throws TransportException.CodecException {
        KafkaHeader kafkaHeader = new KafkaHeader();

        kafkaHeader.setApiKey(buffer.readShort());
        kafkaHeader.setApiVersion(buffer.readShort());
        kafkaHeader.setRequestId(buffer.readInt());
        try {
            kafkaHeader.setClientId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        } catch (Exception e) {
            throw new TransportException.CodecException(e);
        }

        if (kafkaHeader.getDirection() == null) kafkaHeader.setDirection(Direction.REQUEST);
        if (kafkaHeader.getQosLevel() == null) kafkaHeader.setQosLevel(QosLevel.RECEIVE);

        return kafkaHeader;
    }

    @Override
    public void encode(Object obj, ByteBuf buffer) throws TransportException.CodecException {
        KafkaHeader kafkaHeader = (KafkaHeader)obj;
        buffer.writeInt(kafkaHeader.getRequestId());
    }
}
