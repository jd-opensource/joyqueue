package com.jd.journalq.broker.kafka.network.codec;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.network.KafkaPayloadCodec;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.command.ListOffsetsRequest;
import com.jd.journalq.broker.kafka.command.ListOffsetsResponse;
import com.jd.journalq.broker.kafka.model.IsolationLevel;
import com.jd.journalq.broker.kafka.model.PartitionOffsetsResponse;
import com.jd.journalq.broker.kafka.network.KafkaHeader;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;
import java.util.Set;

/**
 * ListOffsetsCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class ListOffsetsCodec implements KafkaPayloadCodec<ListOffsetsResponse>, Type {

    @Override
    public Object decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        ListOffsetsRequest offsetRequest = new ListOffsetsRequest();
        offsetRequest.setReplicaId(buffer.readInt());
        if (header.getVersion() >= 2) {
            offsetRequest.setIsolationLevel(IsolationLevel.valueOf(buffer.readByte()));
        }
        int topics = buffer.readInt();
        Table<TopicName, Integer, ListOffsetsRequest.PartitionOffsetRequestInfo> offsetRequestTable = HashBasedTable.create();
        for (int i = 0; i < topics; i++) {
            TopicName topic = TopicName.parse(Serializer.readString(buffer, Serializer.SHORT_SIZE));
            int partitions = buffer.readInt();
            for (int j = 0; j < partitions; j++) {
                int partitionId = buffer.readInt();
                long time = buffer.readLong();
                int maxNumOffsets = header.getApiVersion() == 0 ? buffer.readInt() : 1;
                ListOffsetsRequest.PartitionOffsetRequestInfo partitionOffsetRequestInfo = offsetRequest.new PartitionOffsetRequestInfo(time, maxNumOffsets);
                offsetRequestTable.put(topic, partitionId, partitionOffsetRequestInfo);
            }
        }
        offsetRequest.setOffsetRequestTable(offsetRequestTable);
        return offsetRequest;
    }

    @Override
    public void encode(ListOffsetsResponse payload, ByteBuf buffer) throws Exception {
        short version = payload.getVersion();
        if (version >= 2) {
            // throttle_time_ms
            buffer.writeInt(payload.getThrottleTimeMs());
        }

        // 响应的map
        Table<String, Integer, PartitionOffsetsResponse> offsetRequestTable = payload.getOffsetsResponseTable();
        Set<String> topics = offsetRequestTable.rowKeySet();
        // 主题大小
        buffer.writeInt(topics.size());
        for (String topic : topics) {
            // 主题
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
            Map<Integer, PartitionOffsetsResponse> partitionOffsetsResponseMap = offsetRequestTable.row(topic);
            Set<Integer> partitions = partitionOffsetsResponseMap.keySet();
            // partition数量
            buffer.writeInt(partitions.size());
            for (int partition : partitions) {
                PartitionOffsetsResponse partitionOffsetsResponse = partitionOffsetsResponseMap.get(partition);
                if (partitionOffsetsResponse != null) {
                    // partition编号
                    buffer.writeInt(partition);
                    // 错误码
                    buffer.writeShort(partitionOffsetsResponse.getErrorCode());
                    // 时间戳
                    if (version >= 1) {
                        buffer.writeLong(partitionOffsetsResponse.getTimestamp());
                    }

                    // 偏移量
                    long offset = partitionOffsetsResponse.getOffset();
                    if (version == 0) {
                        buffer.writeInt(1);
                        buffer.writeLong(offset);
                    } else {
                        buffer.writeLong(offset);
                    }
                }
            }
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.LIST_OFFSETS.getCode();
    }
}