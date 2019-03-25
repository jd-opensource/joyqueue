package com.jd.journalq.broker.kafka.network.protocol;

import com.jd.journalq.broker.kafka.command.KafkaRequestOrResponse;
import com.jd.journalq.broker.kafka.network.KafkaHeader;
import com.jd.journalq.network.transport.codec.DefaultEncoder;
import com.jd.journalq.network.transport.codec.PayloadCodecFactory;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.exception.TransportException;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * kafka编码
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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