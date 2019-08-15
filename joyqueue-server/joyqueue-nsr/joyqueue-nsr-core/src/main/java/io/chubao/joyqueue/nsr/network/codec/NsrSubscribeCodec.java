package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.codec.SubscribeCodec;
import io.chubao.joyqueue.network.command.Subscribe;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;


/**
 * @author wylixiaobin
 * Date: 2019/3/20
 */
public class NsrSubscribeCodec extends SubscribeCodec implements NsrPayloadCodec<Subscribe> {
    @Override
    public int type() {
        return NsrCommandType.SUBSCRIBE;
    }
}
