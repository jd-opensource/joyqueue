package io.chubao.joyqueue.nsr.network;

import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.codec.CodecFactory;
import io.chubao.joyqueue.network.transport.codec.PayloadCodecFactory;
import io.chubao.joyqueue.network.transport.codec.support.JoyQueueCodec;
import io.chubao.joyqueue.nsr.NsrPlugins;

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
        return new JoyQueueCodec(payloadCodecFactory);
    }
}
