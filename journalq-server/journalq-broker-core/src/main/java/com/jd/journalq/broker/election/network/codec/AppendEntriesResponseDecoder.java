package com.jd.journalq.broker.election.network.codec;

import com.jd.journalq.broker.election.command.AppendEntriesResponse;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadDecoder;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/27
 */
public class AppendEntriesResponseDecoder implements PayloadDecoder<JMQHeader>, Type {
    @Override
    public Object decode(final JMQHeader header, final ByteBuf buffer) throws Exception {
        AppendEntriesResponse appendEntriesResponse = new AppendEntriesResponse();

        appendEntriesResponse.setTerm(buffer.readInt());
        appendEntriesResponse.setSuccess(buffer.readBoolean());
        appendEntriesResponse.setNextPosition(buffer.readLong());
        appendEntriesResponse.setWritePosition(buffer.readLong());
        appendEntriesResponse.setReplicaId(buffer.readInt());

        return appendEntriesResponse;
    }

    @Override
    public int type() {
        return CommandType.RAFT_APPEND_ENTRIES_RESPONSE;
    }
}
