package io.chubao.joyqueue.broker.network.codec;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.codec.CodecFactory;
import io.chubao.joyqueue.network.transport.codec.PayloadCodecFactory;
import io.chubao.joyqueue.network.transport.codec.support.JoyQueueCodec;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;


/**
 * BrokerCodecFactory
 *
 * author: gaohaoxiang
 * date: 2018/8/21
 */
public class BrokerCodecFactory implements CodecFactory {

    private static final Codec CODEC_INSTANCE = new BrokerCodecFactory().getCodec();

    private PayloadCodecFactory payloadCodecFactory;

    public BrokerCodecFactory() {
        this.payloadCodecFactory = initPayloadCodecFactory();
    }

    protected PayloadCodecFactory initPayloadCodecFactory() {
        PayloadCodecFactory payloadCodecFactory = new PayloadCodecFactory();
        List<BrokerPayloadCodec> brokerPayloadCodecs = loadPayloadCodecs();
        for (BrokerPayloadCodec brokerPayloadCodec : brokerPayloadCodecs) {
            payloadCodecFactory.register(brokerPayloadCodec);
        }

        // TODO 临时
        BrokerPayloadCodecRegistrar.register(payloadCodecFactory);
        return payloadCodecFactory;
    }

    protected List<BrokerPayloadCodec> loadPayloadCodecs() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(BrokerPayloadCodec.class));
    }

    @Override
    public Codec getCodec() {
        return new JoyQueueCodec(payloadCodecFactory);
    }

    public static Codec getInstance() {
        return CODEC_INSTANCE;
    }
}