package io.chubao.joyqueue.network.codec;

import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.codec.CodecFactory;
import io.chubao.joyqueue.network.transport.codec.support.JoyQueueCodec;

/**
 * JoyQueueCodecFactory
 *
 * author: gaohaoxiang
 * date: 2018/11/28
 */
public class JoyQueueCodecFactory implements CodecFactory {

    @Override
    public Codec getCodec() {
        return new JoyQueueCodec();
    }
}