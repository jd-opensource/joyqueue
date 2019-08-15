package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.codec.SubscribeAckCodec;
import io.chubao.joyqueue.network.command.SubscribeAck;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;

/**
 * @author wylixiaobin
 * Date: 2019/3/20
 */
public class NsrSubscribeAckCodec extends SubscribeAckCodec implements NsrPayloadCodec<SubscribeAck> {
    @Override
    public int type() {
        return NsrCommandType.SUBSCRIBE_ACK;
    }
}
