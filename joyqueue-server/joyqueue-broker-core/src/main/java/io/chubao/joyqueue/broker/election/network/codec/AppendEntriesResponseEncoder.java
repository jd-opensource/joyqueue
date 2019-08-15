package io.chubao.joyqueue.broker.election.network.codec;

import io.chubao.joyqueue.broker.election.command.AppendEntriesResponse;
import io.chubao.joyqueue.network.transport.codec.PayloadEncoder;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/27
 */
public class AppendEntriesResponseEncoder implements PayloadEncoder<AppendEntriesResponse>, Type {
    @Override
    public void encode(final AppendEntriesResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getTerm());
        buffer.writeBoolean(payload.isSuccess());
        buffer.writeLong(payload.getNextPosition());
        buffer.writeLong(payload.getWritePosition());
        buffer.writeInt(payload.getReplicaId());
    }

    @Override
    public int type() {
        return CommandType.RAFT_APPEND_ENTRIES_RESPONSE;
    }
}
