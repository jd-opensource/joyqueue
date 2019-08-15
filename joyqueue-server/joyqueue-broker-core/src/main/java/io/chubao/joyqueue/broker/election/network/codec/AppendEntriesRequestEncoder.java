package io.chubao.joyqueue.broker.election.network.codec;

import io.chubao.joyqueue.broker.election.command.AppendEntriesRequest;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.PayloadEncoder;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/27
 */
public class AppendEntriesRequestEncoder implements PayloadEncoder<AppendEntriesRequest>, Type {
    @Override
    public void encode(final AppendEntriesRequest payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(payload.getPartitionGroup());
        buffer.writeInt(payload.getTerm());
        buffer.writeInt(payload.getLeaderId());

        buffer.writeInt(payload.getPrevTerm());
        buffer.writeLong(payload.getPrevPosition());

        buffer.writeLong(payload.getStartPosition());
        buffer.writeLong(payload.getCommitPosition());
        buffer.writeLong(payload.getLeftPosition());

        buffer.writeBoolean(payload.isMatch());

        ByteBuffer entries = payload.getEntries();
        if (entries == null) {
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(entries.remaining());
        entries.mark();
        buffer.writeBytes(entries);
        entries.reset();
    }

    @Override
    public int type() {
        return CommandType.RAFT_APPEND_ENTRIES_REQUEST;
    }
}
