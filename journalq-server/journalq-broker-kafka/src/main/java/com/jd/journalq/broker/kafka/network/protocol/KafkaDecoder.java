package com.jd.journalq.broker.kafka.network.protocol;

import com.jd.journalq.broker.kafka.command.KafkaRequestOrResponse;
import com.jd.journalq.broker.kafka.network.KafkaHeader;
import com.jd.journalq.network.transport.codec.DefaultDecoder;
import com.jd.journalq.network.transport.codec.PayloadCodecFactory;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.exception.TransportException;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * kafka解码
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class KafkaDecoder extends DefaultDecoder {
    private static Logger logger = LoggerFactory.getLogger(KafkaDecoder.class);

    public KafkaDecoder(KafkaHeaderCodec headerCodec, PayloadCodecFactory payloadCodecFactory) {
        super(headerCodec, payloadCodecFactory);
    }

    @Override
    public Object decode(ByteBuf buffer) throws TransportException.CodecException {
        Command command = (Command) super.decode(buffer);
        if (command != null) {
            fillHeader((KafkaHeader) command.getHeader(), (KafkaRequestOrResponse) command.getPayload());
        }
        return command;
    }

    private void fillHeader(final KafkaHeader header, KafkaRequestOrResponse payload) throws TransportException.CodecException {
        payload.setVersion((short) header.getVersion());
        payload.setCorrelationId(header.getRequestId());
        payload.setClientId(header.getClientId());
        payload.setDirection(header.getDirection());
    }

    protected int readLength(ByteBuf buffer) {
        return buffer.readInt();
    }
}