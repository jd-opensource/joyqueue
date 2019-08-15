package io.chubao.joyqueue.broker.election.network.codec;

import io.chubao.joyqueue.broker.election.command.ReplicateConsumePosResponse;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadDecoder;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/29
 */
public class ReplicateConsumePosResponseDecoder implements PayloadDecoder<JoyQueueHeader>, Type {

    @Override
    public Object decode(final JoyQueueHeader header, final ByteBuf buffer) {
        return new ReplicateConsumePosResponse(buffer.readBoolean());
    }

    @Override
    public int type() {
        return CommandType.REPLICATE_CONSUME_POS_RESPONSE;
    }
}
