package io.chubao.joyqueue.network.transport.codec.support;

import io.chubao.joyqueue.network.command.JoyQueuePayloadCodecRegistry;
import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.codec.Decoder;
import io.chubao.joyqueue.network.transport.codec.DefaultDecoder;
import io.chubao.joyqueue.network.transport.codec.DefaultEncoder;
import io.chubao.joyqueue.network.transport.codec.Encoder;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeaderCodec;
import io.chubao.joyqueue.network.transport.codec.PayloadCodecFactory;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;

/**
 * JoyQueueCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/22
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
        this.decoder = new DefaultDecoder(headerCodec, payloadCodecFactory);
        this.encoder = new DefaultEncoder(headerCodec, payloadCodecFactory);
    }

    public JoyQueueCodec(PayloadCodecFactory payloadCodecFactory) {
        this(new JoyQueueHeaderCodec(), payloadCodecFactory);
    }

    public JoyQueueCodec(Codec headerCodec, PayloadCodecFactory payloadCodecFactory) {
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
