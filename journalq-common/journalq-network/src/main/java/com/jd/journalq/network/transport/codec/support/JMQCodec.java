package com.jd.journalq.network.transport.codec.support;

import com.jd.journalq.network.command.JMQPayloadCodecRegistry;
import com.jd.journalq.network.transport.codec.Codec;
import com.jd.journalq.network.transport.codec.Decoder;
import com.jd.journalq.network.transport.codec.DefaultDecoder;
import com.jd.journalq.network.transport.codec.DefaultEncoder;
import com.jd.journalq.network.transport.codec.Encoder;
import com.jd.journalq.network.transport.codec.JMQHeaderCodec;
import com.jd.journalq.network.transport.codec.PayloadCodecFactory;
import com.jd.journalq.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;

/**
 * JMQCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/22
 */
public class JMQCodec implements Codec {

    private Codec headerCodec;
    private PayloadCodecFactory payloadCodecFactory;

    private Decoder decoder;
    private Encoder encoder;

    public JMQCodec() {
        PayloadCodecFactory payloadCodecFactory = new PayloadCodecFactory();
        JMQPayloadCodecRegistry.register(payloadCodecFactory);
        this.headerCodec = new JMQHeaderCodec();
        this.payloadCodecFactory = payloadCodecFactory;
        this.decoder = new DefaultDecoder(headerCodec, payloadCodecFactory);
        this.encoder = new DefaultEncoder(headerCodec, payloadCodecFactory);
    }

    public JMQCodec(PayloadCodecFactory payloadCodecFactory) {
        this(new JMQHeaderCodec(), payloadCodecFactory);
    }

    public JMQCodec(Codec headerCodec, PayloadCodecFactory payloadCodecFactory) {
        this.headerCodec = headerCodec;
        this.payloadCodecFactory = payloadCodecFactory;
        this.decoder = new DefaultDecoder(headerCodec, payloadCodecFactory);
        this.encoder = new DefaultEncoder(headerCodec, payloadCodecFactory);
    }

    @Override
    public Object decode(ByteBuf buffer) throws TransportException.CodecException {
        return decoder.decode(buffer);
    }

    @Override
    public void encode(Object obj, ByteBuf buffer) throws TransportException.CodecException {
        encoder.encode(obj, buffer);
    }

    public PayloadCodecFactory getPayloadCodecFactory() {
        return payloadCodecFactory;
    }
}
