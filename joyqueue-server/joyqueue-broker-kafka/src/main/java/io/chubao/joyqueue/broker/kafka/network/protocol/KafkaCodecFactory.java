package io.chubao.joyqueue.broker.kafka.network.protocol;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.codec.CodecFactory;
import io.chubao.joyqueue.network.transport.codec.PayloadCodecFactory;
import io.chubao.joyqueue.network.transport.codec.support.DefaultCodec;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * KafkaCodecFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class KafkaCodecFactory implements CodecFactory {

    private PayloadCodecFactory payloadCodecFactory;

    public KafkaCodecFactory() {
        this.payloadCodecFactory = initPayloadCodecFactory();
    }

    protected PayloadCodecFactory initPayloadCodecFactory() {
        PayloadCodecFactory payloadCodecFactory = new PayloadCodecFactory();
        List<KafkaPayloadCodec> payloadCodecs = loadPayloadCodecs();
        for (KafkaPayloadCodec payloadCodec : payloadCodecs) {
            payloadCodecFactory.register(payloadCodec);
        }
        return payloadCodecFactory;
    }

    protected List<KafkaPayloadCodec> loadPayloadCodecs() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(KafkaPayloadCodec.class));
    }

    @Override
    public Codec getCodec() {
        KafkaHeaderCodec headerCodec = new KafkaHeaderCodec();
        return new DefaultCodec(new KafkaDecoder(headerCodec, payloadCodecFactory), new KafkaEncoder(headerCodec, payloadCodecFactory));
    }
}