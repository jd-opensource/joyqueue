package io.chubao.joyqueue.broker.election.network.codec;

import io.chubao.joyqueue.broker.election.command.AppendEntriesResponse;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadDecoder;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/27
 */
public class AppendEntriesResponseDecoder implements PayloadDecoder<JoyQueueHeader>, Type {
    @Override
    public Object decode(final JoyQueueHeader header, final ByteBuf buffer) throws Exception {
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
