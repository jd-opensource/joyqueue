package com.jd.joyqueue.broker.protocol.network.codec;

import com.jd.joyqueue.network.command.JoyQueuePayloadCodecRegistry;
import com.jd.joyqueue.network.transport.codec.Codec;
import com.jd.joyqueue.network.transport.codec.Decoder;
import com.jd.joyqueue.network.transport.codec.DefaultDecoder;
import com.jd.joyqueue.network.transport.codec.DefaultEncoder;
import com.jd.joyqueue.network.transport.codec.Encoder;
import com.jd.joyqueue.network.transport.codec.JoyQueueHeaderCodec;
import com.jd.joyqueue.network.transport.codec.PayloadCodecFactory;
import com.jd.joyqueue.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;

/**
 * JoyQueueCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/6/27
 */
public class JoyQueueCodec implements Codec {

    private Codec headerCodec;
    private PayloadCodecFactory payloadCodecFactory;

    private Decoder decoder;
    private Encoder encoder;

    public JoyQueueCodec() {
        PayloadCodecFactory payloadCodecFactory = new PayloadCodecFactory();
        JoyQueuePayloadCodecRegistry.register(payloadCodecFactory);
        this.headerCodec = new JoyQueueHeaderCodec();
        this.payloadCodecFactory = payloadCodecFactory;
        this.decoder = new JoyQueueDecoder(headerCodec, payloadCodecFactory);
        this.encoder = new JoyQueueEncoder(headerCodec, payloadCodecFactory);
    }

    public JoyQueueCodec(PayloadCodecFactory payloadCodecFactory) {
        this(new JoyQueueHeaderCodec(), payloadCodecFactory);
    }

    public JoyQueueCodec(Codec headerCodec, PayloadCodecFactory payloadCodecFactory) {
        this.headerCodec = headerCodec;
        this.payloadCodecFactory = payloadCodecFactory;
        this.decoder = new JoyQueueDecoder(headerCodec, payloadCodecFactory);
        this.encoder = new JoyQueueEncoder(headerCodec, payloadCodecFactory);
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