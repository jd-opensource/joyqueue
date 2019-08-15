package io.chubao.joyqueue.broker.election.network.codec;

import io.chubao.joyqueue.network.transport.codec.PayloadEncoder;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.broker.election.command.TimeoutNowResponse;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/10/2
 */
public class TimeoutNowResponseEncoder implements PayloadEncoder<TimeoutNowResponse>, Type {
    @Override
    public void encode(final TimeoutNowResponse response, ByteBuf buffer) throws Exception {
        buffer.writeBoolean(response.isSuccess());
        buffer.writeInt(response.getTerm());
    }

    @Override
    public int type() {
        return CommandType.RAFT_TIMEOUT_NOW_RESPONSE;
    }
}
