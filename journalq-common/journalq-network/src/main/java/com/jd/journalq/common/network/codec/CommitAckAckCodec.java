package com.jd.journalq.common.network.codec;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.network.command.CommitAckAck;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * CommitAckAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public class CommitAckAckCodec implements PayloadCodec<JMQHeader, CommitAckAck>, Type {

    @Override
    public CommitAckAck decode(JMQHeader header, ByteBuf buffer) throws Exception {
        short size = buffer.readShort();
        Table<String, Short, JMQCode> result = HashBasedTable.create();

        for (int i = 0; i < size; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            short partitionSize = buffer.readShort();
            for (int j = 0; j < partitionSize; j++) {
                short partition = buffer.readShort();
                result.put(topic, partition, JMQCode.valueOf(buffer.readInt()));
            }
        }

        CommitAckAck commitAckAck = new CommitAckAck();
        commitAckAck.setResult(result);
        return commitAckAck;
    }

    @Override
    public void encode(CommitAckAck payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getResult().size());
        for (Map.Entry<String, Map<Short, JMQCode>> entry : payload.getResult().rowMap().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeShort(entry.getValue().size());
            for (Map.Entry<Short, JMQCode> partitionEntry : entry.getValue().entrySet()) {
                buffer.writeShort(partitionEntry.getKey());
                buffer.writeInt(partitionEntry.getValue().getCode());
            }
        }
    }

    @Override
    public int type() {
        return JMQCommandType.COMMIT_ACK_ACK.getCode();
    }
}