package io.chubao.joyqueue.network.transport.codec.support;

import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.codec.CodecFactory;
import io.chubao.joyqueue.network.transport.codec.Decoder;
import io.chubao.joyqueue.network.transport.codec.Encoder;

/**
 * DefaultCodecFactory
 *
 * author: gaohaoxiang
 * date: 2018/8/13
 */
public class DefaultCodecFactory implements CodecFactory {

    private Decoder decoder;
    private Encoder encoder;

    public DefaultCodecFactory(Decoder decoder, Encoder encoder) {
        this.decoder = decoder;
        this.encoder = encoder;
    }

    @Override
    public Codec getCodec() {
        return new DefaultCodec(decoder, encoder);
    }
}