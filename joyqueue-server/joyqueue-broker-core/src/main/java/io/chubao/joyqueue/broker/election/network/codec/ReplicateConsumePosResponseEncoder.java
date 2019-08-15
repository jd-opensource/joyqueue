package io.chubao.joyqueue.broker.election.network.codec;

import io.chubao.joyqueue.broker.election.command.ReplicateConsumePosResponse;
import io.chubao.joyqueue.network.transport.codec.PayloadEncoder;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/29
 */
public class ReplicateConsumePosResponseEncoder implements PayloadEncoder<ReplicateConsumePosResponse>, Type {

    @Override
    public void encode(final ReplicateConsumePosResponse response, ByteBuf buffer) {
        buffer.writeBoolean(response.isSuccess());
    }

    @Override
    public int type() {
        return CommandType.REPLICATE_CONSUME_POS_RESPONSE;
    }
}
