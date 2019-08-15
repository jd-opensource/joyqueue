package io.chubao.joyqueue.broker.election.network.codec;

import io.chubao.joyqueue.network.transport.codec.PayloadEncoder;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.broker.election.command.TimeoutNowRequest;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/10/2
 */
public class TimeoutNowRequestEncoder implements PayloadEncoder<TimeoutNowRequest>, Type {
    @Override
    public void encode(final TimeoutNowRequest request, ByteBuf buffer) throws Exception {
        Serializer.write(request.getTopic(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(request.getPartitionGroup());
        buffer.writeInt(request.getTerm());
    }

    @Override
    public int type() {
        return CommandType.RAFT_TIMEOUT_NOW_REQUEST;
    }
}
