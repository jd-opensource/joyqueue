package com.jd.journalq.broker.index.network.codec;

import com.jd.journalq.broker.index.command.ConsumeIndexQueryResponse;
import com.jd.journalq.broker.index.model.IndexMetadataAndError;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadDecoder;
import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Type;

import io.netty.buffer.ByteBuf;
import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class IndexQueryResponseDecoder implements PayloadDecoder<JMQHeader>, Type {

    @Override
    public Object decode(final JMQHeader header, final ByteBuf buffer) throws Exception {
        Map<String, Map<Integer, IndexMetadataAndError>> topicPartitionIndex = new HashedMap();
        int topics = buffer.readInt();
        for (int i = 0; i < topics; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int partitions = buffer.readInt();
            Map<Integer, IndexMetadataAndError> partitionIndexs = new HashedMap();
            for (int j = 0; j < partitions; j++) {
                int partition = buffer.readInt();
                long index = buffer.readLong();
                String metadata = Serializer.readString(buffer, Serializer.SHORT_SIZE);
                short error = buffer.readShort();
                partitionIndexs.put(partition, new IndexMetadataAndError(index, metadata, error));
            }
            topicPartitionIndex.put(topic, partitionIndexs);
        }
        return new ConsumeIndexQueryResponse(topicPartitionIndex);
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_QUERY_RESPONSE;
    }
}
