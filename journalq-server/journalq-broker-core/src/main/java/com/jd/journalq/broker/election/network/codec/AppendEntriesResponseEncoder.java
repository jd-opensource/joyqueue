package com.jd.journalq.broker.election.network.codec;

import com.jd.journalq.broker.election.command.AppendEntriesResponse;
import com.jd.journalq.common.network.transport.codec.PayloadEncoder;
import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.transport.command.Type;
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
