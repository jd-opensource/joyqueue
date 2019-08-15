package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.codec.UnSubscribeCodec;
import io.chubao.joyqueue.network.command.UnSubscribe;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;

/**
 * @author wylixiaobin
 * Date: 2019/3/20
 */
public class NsrUnSubscribeCodec extends UnSubscribeCodec implements NsrPayloadCodec<UnSubscribe> {
    @Override
    public int type() {
        return NsrCommandType.UN_SUBSCRIBE;
    }
}
