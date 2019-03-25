package com.jd.journalq.broker.kafka.network.codec;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.network.KafkaHeader;
import com.jd.journalq.broker.kafka.network.KafkaPayloadCodec;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.command.FetchRequest;
import com.jd.journalq.broker.kafka.command.FetchResponse;
import com.jd.journalq.broker.kafka.message.KafkaMessageSerializer;
import com.jd.journalq.broker.kafka.model.FetchResponsePartitionData;
import com.jd.journalq.broker.kafka.model.IsolationLevel;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;

import java.util.Map;
import java.util.Set;

/**
 * FetchCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class FetchCodec implements KafkaPayloadCodec<FetchResponse>, Type {

    @Override
    public Object decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        FetchRequest fetchRequest = new FetchRequest();
        fetchRequest.setReplicaId(buffer.readInt());
        fetchRequest.setMaxWait(buffer.readInt());
        fetchRequest.setMinBytes(buffer.readInt());
        if (header.getApiVersion() >= 3) {
            // max_bytes
            fetchRequest.setMaxBytes(buffer.readInt());
        }
        if (header.getApiVersion() >= 4) {
            // isolation_level
            fetchRequest.setIsolationLevel(IsolationLevel.valueOf(buffer.readByte()));
        }
        int topicCount = buffer.readInt();
        int numPartitions = 0;
        Table<TopicName, Integer, FetchRequest.PartitionFetchInfo> topicFetchInfoTable = HashBasedTable.create();
        for (int i = 0; i < topicCount; i++) {
            TopicName topic = TopicName.parse(Serializer.readString(buffer, Serializer.SHORT_SIZE));
            int partitionCount = buffer.readInt();
            // 计算partition总数
            numPartitions += partitionCount;
            for (int j = 0; j < partitionCount; j++) {
                int partitionId = buffer.readInt();
                long offset = buffer.readLong();
                long logStartOffset = 0;
                if (header.getApiVersion() >= 5) {
                    // log_start_offset
                    logStartOffset = buffer.readLong();
                }
                int partitionMaxBytes = buffer.readInt();
                FetchRequest.PartitionFetchInfo partitionFetchInfo = fetchRequest.new PartitionFetchInfo(offset, partitionMaxBytes);
                if (header.getApiVersion() >= 5) {
                    partitionFetchInfo.setLogStartOffset(logStartOffset);
                }
                topicFetchInfoTable.put(topic, partitionId, partitionFetchInfo);
            }
        }
        fetchRequest.setNumPartitions(numPartitions);
        fetchRequest.setRequestInfo(topicFetchInfoTable);
        return fetchRequest;
    }

    @Override
    public void encode(FetchResponse payload, ByteBuf buffer) throws Exception {
        short version = payload.getVersion();
        if (version >= 1) {
            buffer.writeInt(payload.getThrottleTimeMs());
        }
        Table<String, Integer, FetchResponsePartitionData> fetchDataTable = payload.getFetchResponses();
        Set<String> topics = fetchDataTable.rowKeySet();
        buffer.writeInt(topics.size());
        for (String topic : topics) {
            try {
                Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
            } catch (Exception e) {
                throw new TransportException.CodecException(e);
            }
            Map<Integer, FetchResponsePartitionData> partitionDataMap = fetchDataTable.row(topic);
            Set<Integer> partitions = partitionDataMap.keySet();
            buffer.writeInt(partitions.size());

            for (int partition : partitions) {
                FetchResponsePartitionData fetchResponsePartitionData = partitionDataMap.get(partition);
                buffer.writeInt(partition);
                buffer.writeShort(fetchResponsePartitionData.getError());
                buffer.writeLong(fetchResponsePartitionData.getHw());

                // not fully supported, just make it compatible
                if (version >= 4) {
                    // last_stable_offset
                    buffer.writeLong(0);

                    // log_start_offset
                    if (version >= 5) {
                        buffer.writeLong(0);
                    }

                    // aborted_transactions
                    // size
                    buffer.writeInt(0);
                    // producer_id
                    // first_offset
                }

                int startIndex = buffer.writerIndex();
                buffer.writeInt(0); // length

                KafkaMessageSerializer.writeMessages(buffer, fetchResponsePartitionData.getMessages());

                int length = buffer.writerIndex() - startIndex - 4;
                buffer.markWriterIndex();
                buffer.writerIndex(startIndex);
                buffer.writeInt(length);
                buffer.resetWriterIndex();
            }
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.FETCH.getCode();
    }
}