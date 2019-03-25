package com.jd.journalq.common.network.codec;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.network.command.FetchAssignedPartitionAck;
import com.jd.journalq.common.network.command.FetchAssignedPartitionAckData;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * FetchAssignedPartitionAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class FetchAssignedPartitionAckCodec implements PayloadCodec<JMQHeader, FetchAssignedPartitionAck>, Type {

    @Override
    public FetchAssignedPartitionAck decode(JMQHeader header, ByteBuf buffer) throws Exception {
        FetchAssignedPartitionAck fetchAssignedPartitionAck = new FetchAssignedPartitionAck();
        Map<String, FetchAssignedPartitionAckData> topicPartitions = Maps.newHashMap();

        short topicSize = buffer.readShort();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);

            int partitionSize = buffer.readShort();
            List<Short> partitions = Lists.newArrayListWithCapacity(partitionSize);
            for (int j = 0; j < partitionSize; j++) {
                partitions.add(buffer.readShort());
            }

            JMQCode code = JMQCode.valueOf(buffer.readInt());
            topicPartitions.put(topic, new FetchAssignedPartitionAckData(partitions, code));
        }

        fetchAssignedPartitionAck.setTopicPartitions(topicPartitions);
        return fetchAssignedPartitionAck;
    }

    @Override
    public void encode(FetchAssignedPartitionAck payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getTopicPartitions().size());
        for (Map.Entry<String, FetchAssignedPartitionAckData> entry : payload.getTopicPartitions().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            FetchAssignedPartitionAckData data = entry.getValue();
            buffer.writeShort(data.getPartitions().size());
            for (Short partition : data.getPartitions()) {
                buffer.writeShort(partition);
            }
            buffer.writeInt(data.getCode().getCode());
        }
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_ASSIGNED_PARTITION_ACK.getCode();
    }
}