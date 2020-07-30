package org.joyqueue.network.codec;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.netty.buffer.ByteBuf;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.CommitIndexResponse;
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
public class CommitIndexResponseCodec implements PayloadCodec<JoyQueueHeader, CommitIndexResponse>, Type {

    @Override
    public CommitIndexResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        short size = buffer.readShort();
        Table<String, Short, JoyQueueCode> result = HashBasedTable.create();

        for (int i = 0; i < size; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            short partitionSize = buffer.readShort();
            for (int j = 0; j < partitionSize; j++) {
                short partition = buffer.readShort();
                result.put(topic, partition, JoyQueueCode.valueOf(buffer.readInt()));
            }
        }

        CommitIndexResponse commitIndexResponse = new CommitIndexResponse();
        commitIndexResponse.setResult(result);
        return commitIndexResponse;
    }

    @Override
    public void encode(CommitIndexResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getResult().size());
        for (Map.Entry<String, Map<Short, JoyQueueCode>> entry : payload.getResult().rowMap().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeShort(entry.getValue().size());
            for (Map.Entry<Short, JoyQueueCode> partitionEntry : entry.getValue().entrySet()) {
                buffer.writeShort(partitionEntry.getKey());
                buffer.writeInt(partitionEntry.getValue().getCode());
            }
        }
    }

    @Override
    public int type() {
        return JoyQueueCommandType.COMMIT_ACK_INDEX_RESPONSE.getCode();
    }
}