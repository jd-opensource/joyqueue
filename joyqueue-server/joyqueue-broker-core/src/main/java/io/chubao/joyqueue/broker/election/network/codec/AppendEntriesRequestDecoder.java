package io.chubao.joyqueue.broker.election.network.codec;

import io.chubao.joyqueue.broker.election.TopicPartitionGroup;
import io.chubao.joyqueue.broker.election.command.AppendEntriesRequest;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadDecoder;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/27
 */
public class AppendEntriesRequestDecoder implements PayloadDecoder<JoyQueueHeader>, Type {
    @Override
    public Object decode(final JoyQueueHeader header, final ByteBuf buffer) throws Exception {

        AppendEntriesRequest request = new AppendEntriesRequest();

        String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
        int partitionGroupId = buffer.readInt();
        request.setTopicPartitionGroup(new TopicPartitionGroup(topic, partitionGroupId));
        request.setTerm(buffer.readInt());
        request.setLeaderId(buffer.readInt());

        request.setPrevTerm(buffer.readInt());
        request.setPrevPosition(buffer.readLong());

        request.setStartPosition(buffer.readLong());
        request.setCommitPosition(buffer.readLong());
        request.setLeftPosition(buffer.readLong());

        request.setMatch(buffer.readBoolean());

        int length = buffer.readInt();
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes, 0, length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.rewind();

        /*
        int length = buffer.readInt();
        ByteBuffer byteBuffer = buffer.nioBuffer(buffer.readerIndex(), length);
        buffer.readerIndex(buffer.readerIndex() + length);
        */

        request.setEntries(byteBuffer);
        return request;

    }

    @Override
    public int type() {
        return CommandType.RAFT_APPEND_ENTRIES_REQUEST;
    }
}
