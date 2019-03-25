package com.jd.journalq.broker.network.codec;

import com.google.common.collect.Lists;
import com.jd.journalq.network.transport.codec.Codec;
import com.jd.journalq.network.transport.codec.CodecFactory;
import com.jd.journalq.network.transport.codec.PayloadCodecFactory;
import com.jd.journalq.network.transport.codec.support.JMQCodec;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;


/**
 * 编解码器工厂
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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
        return new JMQCodec(payloadCodecFactory);
    }

    public static Codec getInstance() {
        return CODEC_INSTANCE;
    }
}