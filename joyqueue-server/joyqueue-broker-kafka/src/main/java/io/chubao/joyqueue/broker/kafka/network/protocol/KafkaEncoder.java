package io.chubao.joyqueue.broker.kafka.network.protocol;

import io.chubao.joyqueue.broker.kafka.command.KafkaRequestOrResponse;
import io.chubao.joyqueue.broker.kafka.network.KafkaHeader;
import io.chubao.joyqueue.network.transport.codec.DefaultEncoder;
import io.chubao.joyqueue.network.transport.codec.PayloadCodecFactory;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.exception.TransportException;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * kafka编码
 *
 * author: gaohaoxiang
 * date: 2018/8/21
 */
public class KafkaEncoder extends DefaultEncoder {
    private static Logger logger = LoggerFactory.getLogger(KafkaEncoder.class);

    public KafkaEncoder(KafkaHeaderCodec headerCodec, PayloadCodecFactory payloadCodecFactory) {
        super(headerCodec, payloadCodecFactory);
    }

    @Override
    public void encode(Object obj, ByteBuf buffer) throws TransportException.CodecException {
        Command command = (Command) obj;
        fillHeader((KafkaHeader) command.getHeader(), (KafkaRequestOrResponse) command.getPayload());
        super.encode(obj, buffer);
    }

    public void fillHeader(KafkaHeader header, KafkaRequestOrResponse payload) {
        payload.setVersion((short) header.getVersion());
        payload.setCorrelationId(header.getRequestId());
        payload.setClientId(header.getClientId());
        payload.setDirection(header.getDirection());
    }

    @Override
    protected void writeLength(Object obj, ByteBuf buffer) {
        buffer.setInt(0, buffer.writerIndex() - 4);
    }
}