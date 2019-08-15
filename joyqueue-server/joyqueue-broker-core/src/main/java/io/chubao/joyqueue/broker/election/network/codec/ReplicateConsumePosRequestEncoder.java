package io.chubao.joyqueue.broker.election.network.codec;

import io.chubao.joyqueue.broker.election.command.ReplicateConsumePosRequest;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.PayloadEncoder;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/29
 */
public class ReplicateConsumePosRequestEncoder implements PayloadEncoder<ReplicateConsumePosRequest>, Type {
    @Override
    public void encode(final ReplicateConsumePosRequest request, ByteBuf buffer) throws Exception {
//        Serializer.write(request.getConsumePositions(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(request.getConsumePositions(), buffer, Serializer.INT_SIZE);
    }

    @Override
    public int type() {
        return CommandType.REPLICATE_CONSUME_POS_REQUEST;
    }
}
