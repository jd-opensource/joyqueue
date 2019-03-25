package com.jd.journalq.broker.jmq.network;

import com.google.common.collect.Lists;
import com.jd.journalq.network.transport.codec.Codec;
import com.jd.journalq.network.transport.codec.CodecFactory;
import com.jd.journalq.network.transport.codec.support.JMQCodec;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * JMQCodecFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/28
 */
public class JMQCodecFactory implements CodecFactory {

    private JMQCodec codec;

    public JMQCodecFactory() {
        this.codec = initCodec();
    }

    protected JMQCodec initCodec() {
        JMQCodec codec = new JMQCodec();
        List<JMQPayloadCodec> payloadCodecs = loadPayloadCodecs();
        for (JMQPayloadCodec payloadCodec : payloadCodecs) {
            codec.getPayloadCodecFactory().register(payloadCodec);
        }
        return codec;
    }

    protected List<JMQPayloadCodec> loadPayloadCodecs() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(JMQPayloadCodec.class));
    }

    @Override
    public Codec getCodec() {
        return codec;
    }
}