package com.jd.joyqueue.broker.protocol.network.codec;

import com.jd.joyqueue.network.transport.codec.Codec;
import com.jd.joyqueue.network.transport.codec.CodecFactory;

public class JoyQueueCodecFactory implements CodecFactory {

    @Override
    public Codec getCodec() {
        return new JoyQueueCodec();
    }
}