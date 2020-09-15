package org.joyqueue.network.codec;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.command.CommitIndexRequest;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;

import java.util.Map;

/**
 * CommitIndexRequestCodec
 * author: gaohaoxiang
 * date: 2020/5/20
 */
public class CommitIndexRequestCodec implements PayloadCodec<JoyQueueHeader, CommitIndexRequest>, Type {

    @Override
    public CommitIndexRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        short size = buffer.readShort();
        Table<String, Short, Long> data = HashBasedTable.create();

        for (int i = 0; i < size; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            short partitionSize = buffer.readShort();
            for (int j = 0; j < partitionSize; j++) {
                short partition = buffer.readShort();
                long index = buffer.readLong();
                data.put(topic, partition, index);
            }
        }

        CommitIndexRequest commitIndexRequest = new CommitIndexRequest();
        commitIndexRequest.setData(data);
        commitIndexRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return commitIndexRequest;
    }

    @Override
    public void encode(CommitIndexRequest payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getData().rowMap().size());
        for (Map.Entry<String, Map<Short, Long>> entry : payload.getData().rowMap().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeShort(entry.getValue().size());
            for (Map.Entry<Short, Long> partitionEntry : entry.getValue().entrySet()) {
                buffer.writeShort(partitionEntry.getKey());
                buffer.writeLong(partitionEntry.getValue());
            }
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JoyQueueCommandType.COMMIT_ACK_INDEX_REQUEST.getCode();
    }
}