package com.jd.journalq.network.codec;

import com.jd.journalq.network.transport.codec.Codec;
import com.jd.journalq.network.transport.codec.CodecFactory;
import com.jd.journalq.network.transport.codec.support.JMQCodec;

/**
 * JMQCodecFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/28
 */
public class JMQCodecFactory implements CodecFactory {

    @Override
    public Codec getCodec() {
        return new JMQCodec();
    }
}