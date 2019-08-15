package io.chubao.joyqueue.broker.protocol.network.codec;

import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.codec.CodecFactory;

public class JoyQueueCodecFactory implements CodecFactory {

    @Override
    public Codec getCodec() {
        return new JoyQueueCodec();
    }
}