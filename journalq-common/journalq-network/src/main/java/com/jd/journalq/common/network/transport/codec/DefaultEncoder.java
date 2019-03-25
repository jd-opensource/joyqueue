package com.jd.journalq.common.network.transport.codec;

import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.Header;
import com.jd.journalq.common.network.transport.command.Payload;
import com.jd.journalq.common.network.transport.exception.TransportException;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * jmq编码
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class DefaultEncoder implements Encoder {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultEncoder.class);

    private Codec headerCodec;
    private PayloadCodecFactory payloadCodecFactory;

    public DefaultEncoder(Codec headerCodec, PayloadCodecFactory payloadCodecFactory) {
        this.headerCodec = headerCodec;
        this.payloadCodecFactory = payloadCodecFactory;
    }

    @Override
    public void encode(Object obj, ByteBuf buffer) throws TransportException.CodecException {
        try {
            if (!(obj instanceof Command)) {
                throw new TransportException.CodecException(String.format("unsupported encode type, type: %s", obj.getClass()));
            }

            Command response = (Command) obj;
            Header header = response.getHeader();
            Object payload = response.getPayload();

            buffer.writeInt(0);

            if (payload != null) {
                if (!(payload instanceof Payload)) {
                    throw new TransportException.CodecException(String.format("unsupported encode payload type, payload: %s", payload));
                }

                headerCodec.encode(header, buffer);

                PayloadEncoder encoder = payloadCodecFactory.getEncoder(header);
                if (encoder == null) {
                    throw new TransportException.CodecException(String.format("unsupported encode payload type, header: %s", header));
                }
                encoder.encode((Payload) payload, buffer);
            } else {
                headerCodec.encode(header, buffer);
            }

            writeLength(obj, buffer);

        } catch (Exception e) {
            logger.error("encode exception, payload: {}", obj, e);
            throw new TransportException.CodecException(e.getMessage());
        }
    }

    protected void writeLength(Object obj, ByteBuf buffer) {
        buffer.setInt(0, buffer.writerIndex());
    }
}