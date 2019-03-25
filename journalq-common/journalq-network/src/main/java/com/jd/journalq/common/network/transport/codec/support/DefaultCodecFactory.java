package com.jd.journalq.common.network.transport.codec.support;

import com.jd.journalq.common.network.transport.codec.Codec;
import com.jd.journalq.common.network.transport.codec.CodecFactory;
import com.jd.journalq.common.network.transport.codec.Decoder;
import com.jd.journalq.common.network.transport.codec.Encoder;

/**
 * 默认编解码器工厂
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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