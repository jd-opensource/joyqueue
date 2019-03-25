package com.jd.journalq.network.codec;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.jd.journalq.network.command.CommitAck;
import com.jd.journalq.network.command.CommitAckData;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.command.RetryType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * CommitAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public class CommitAckCodec implements PayloadCodec<JMQHeader, CommitAck>, Type {

    @Override
    public CommitAck decode(JMQHeader header, ByteBuf buffer) throws Exception {
        short size = buffer.readShort();
        Table<String, Short, List<CommitAckData>> data = HashBasedTable.create();

        for (int i = 0; i < size; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            short partitionSize = buffer.readShort();
            for (int j = 0; j < partitionSize; j++) {
                short partition = buffer.readShort();
                short dataSize = buffer.readShort();
                List<CommitAckData> dataList = Lists.newArrayListWithCapacity(dataSize);
                for (int k = 0; k < dataSize; k++) {
                    CommitAckData commitAckData = new CommitAckData();
                    commitAckData.setPartition(buffer.readShort());
                    commitAckData.setIndex(buffer.readLong());
                    commitAckData.setRetryType(RetryType.valueOf(buffer.readByte()));
                    dataList.add(commitAckData);
                }
                data.put(topic, partition, dataList);
            }
        }

        CommitAck commitAck = new CommitAck();
        commitAck.setData(data);
        commitAck.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return commitAck;
    }

    @Override
    public void encode(CommitAck payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getData().rowMap().size());
        for (Map.Entry<String, Map<Short, List<CommitAckData>>> entry : payload.getData().rowMap().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeShort(entry.getValue().size());
            for (Map.Entry<Short, List<CommitAckData>> partitionEntry : entry.getValue().entrySet()) {
                buffer.writeShort(partitionEntry.getKey());
                buffer.writeShort(partitionEntry.getValue().size());
                for (CommitAckData commitAckData : partitionEntry.getValue()) {
                    buffer.writeShort(commitAckData.getPartition());
                    buffer.writeLong(commitAckData.getIndex());
                    buffer.writeByte(commitAckData.getRetryType().getType());
                }
            }
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JMQCommandType.COMMIT_ACK.getCode();
    }
}