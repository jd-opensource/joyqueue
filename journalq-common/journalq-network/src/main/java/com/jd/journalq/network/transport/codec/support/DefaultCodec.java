package com.jd.journalq.network.transport.codec.support;

import com.jd.journalq.network.transport.codec.Codec;
import com.jd.journalq.network.transport.codec.Decoder;
import com.jd.journalq.network.transport.codec.Encoder;
import com.jd.journalq.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;

/**
 * 默认编解码器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public class DefaultCodec implements Codec {

    private Decoder decoder;
    private Encoder encoder;

    public DefaultCodec(Decoder decoder, Encoder encoder) {
        this.decoder = decoder;
        this.encoder = encoder;
    }

    @Override
    public Object decode(ByteBuf buffer) throws TransportException.CodecException {
        return decoder.decode(buffer);
    }

    @Override
    public void encode(Object obj, ByteBuf buffer) throws TransportException.CodecException {
        encoder.encode(obj, buffer);
    }
}