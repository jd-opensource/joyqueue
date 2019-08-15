package io.chubao.joyqueue.broker.protocol.network.codec;

import io.chubao.joyqueue.network.command.JoyQueuePayloadCodecRegistry;
import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.codec.Decoder;
import io.chubao.joyqueue.network.transport.codec.Encoder;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeaderCodec;
import io.chubao.joyqueue.network.transport.codec.PayloadCodecFactory;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;

/**
 * JoyQueueCodec
 *
 * author: gaohaoxiang
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