package com.jd.journalq.broker.index.network.codec;

import com.jd.journalq.broker.index.command.ConsumeIndexQueryResponse;
import com.jd.journalq.broker.index.model.IndexMetadataAndError;
import com.jd.journalq.common.network.transport.codec.PayloadEncoder;
import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Type;

import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class IndexQueryResponseEncoder implements PayloadEncoder<ConsumeIndexQueryResponse>, Type {

    @Override
    public void encode(final ConsumeIndexQueryResponse response, final ByteBuf buffer) throws Exception {
        Map<String, Map<Integer, IndexMetadataAndError>> topicPartitionIndex = response.getTopicPartitionIndex();
        buffer.writeInt(topicPartitionIndex.size());
        for (String topic : topicPartitionIndex.keySet()) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
            Map<Integer, IndexMetadataAndError> partitionIndexs = topicPartitionIndex.get(topic);
            buffer.writeInt(partitionIndexs.size());
            for (int partition : partitionIndexs.keySet()) {
                buffer.writeInt(partition);
                IndexMetadataAndError index = partitionIndexs.get(partition);
                buffer.writeLong(index.getIndex());
                Serializer.write(index.getMetadata(), buffer, Serializer.SHORT_SIZE);
                buffer.writeShort(index.getError());
            }
        }
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_QUERY_RESPONSE;
    }
}
