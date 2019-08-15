package io.chubao.joyqueue.broker.protocol.network;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.broker.protocol.network.codec.JoyQueueCodec;
import io.chubao.joyqueue.broker.protocol.network.codec.JoyQueuePayloadCodec;
import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.codec.CodecFactory;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * JoyQueueCodecFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/28
 */
public class JoyQueueCodecFactory implements CodecFactory {

    private JoyQueueCodec codec;

    public JoyQueueCodecFactory() {
        this.codec = initCodec();
    }

    protected JoyQueueCodec initCodec() {
        JoyQueueCodec codec = new JoyQueueCodec();
        List<JoyQueuePayloadCodec> payloadCodecs = loadPayloadCodecs();
        for (JoyQueuePayloadCodec payloadCodec : payloadCodecs) {
            codec.getPayloadCodecFactory().register(payloadCodec);
        }
        return codec;
    }

    protected List<JoyQueuePayloadCodec> loadPayloadCodecs() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(JoyQueuePayloadCodec.class));
    }

    @Override
    public Codec getCodec() {
        return codec;
    }
}