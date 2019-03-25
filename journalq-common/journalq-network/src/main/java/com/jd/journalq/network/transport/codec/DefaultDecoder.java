package com.jd.journalq.network.transport.codec;

import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * jmq解码
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class DefaultDecoder implements Decoder {

    // LENGTH
    private static final int LENGTH_FIELD_LENGTH = 4;

    protected static final Logger logger = LoggerFactory.getLogger(DefaultDecoder.class);

    private Codec headerCodec;
    private PayloadCodecFactory payloadCodecFactory;

    public DefaultDecoder(Codec headerCodec, PayloadCodecFactory payloadCodecFactory) {
        this.headerCodec = headerCodec;
        this.payloadCodecFactory = payloadCodecFactory;
    }

    @Override
    public Object decode(ByteBuf buffer) throws TransportException.CodecException {
        try {
            if (!buffer.isReadable(LENGTH_FIELD_LENGTH)) {
                return null;
            }
            buffer.markReaderIndex();
            int length = readLength(buffer);
            if (buffer.readableBytes() < length) {
                buffer.resetReaderIndex();
                return null;
            }
            return doDecode(buffer);
        } catch (Exception e) {
            logger.error("decode exception", e);
            throw new TransportException.CodecException(e.getMessage());
        }
    }

    public Object doDecode(ByteBuf buffer) throws Exception {
        Header header = (Header) headerCodec.decode(buffer);
        if (header == null) {
            return null;
        }

        PayloadDecoder decoder = payloadCodecFactory.getDecoder(header);
        if (decoder == null) {
            throw new TransportException.CodecException(String.format("unsupported decode payload type,header: %s", header));
        }

        Object payload = decoder.decode(header, buffer);
        return new Command(header, payload);

    }

    protected int readLength(ByteBuf buffer) {
        return buffer.readInt() - 4;
    }
}