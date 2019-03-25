package com.jd.journalq.nsr.network;

import com.jd.journalq.common.network.transport.codec.Codec;
import com.jd.journalq.common.network.transport.codec.CodecFactory;
import com.jd.journalq.common.network.transport.codec.PayloadCodecFactory;
import com.jd.journalq.common.network.transport.codec.support.JMQCodec;
import com.jd.journalq.nsr.NsrPlugins;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class NsrCodecFactory implements CodecFactory {
    private static final Codec CODEC_INSTANCE = new NsrCodecFactory().getCodec();

    public static Codec getInstance() {
        return CODEC_INSTANCE;
    }

    @Override
    public Codec getCodec() {
        PayloadCodecFactory payloadCodecFactory = new PayloadCodecFactory();
        NsrPlugins.nsrPayloadCodecPlugins.extensions().forEach(nsrPayloadCodec -> payloadCodecFactory.register(nsrPayloadCodec));
        return new JMQCodec(payloadCodecFactory);
    }
}
