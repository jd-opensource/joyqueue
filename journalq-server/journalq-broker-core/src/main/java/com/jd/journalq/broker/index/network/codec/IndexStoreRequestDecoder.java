package com.jd.journalq.broker.index.network.codec;

import com.jd.journalq.broker.index.command.ConsumeIndexStoreRequest;
import com.jd.journalq.broker.index.model.IndexAndMetadata;
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
public class IndexStoreRequestDecoder implements PayloadDecoder<JMQHeader>, Type {

    @Override
    public Object decode(final JMQHeader header, final ByteBuf buffer) throws Exception {
        Map<String, Map<Integer, IndexAndMetadata>> indexMetadata = new HashedMap();
        String app = Serializer.readString(buffer, Serializer.SHORT_SIZE);
        int topics = buffer.readInt();
        for (int i = 0; i < topics; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            Map<Integer, IndexAndMetadata> partitionMetadata = new HashedMap();
            int partitions = buffer.readInt();
            for (int j = 0; j < partitions; j++) {
                int partition = buffer.readInt();
                long index = buffer.readLong();
                String metadata = Serializer.readString(buffer, Serializer.SHORT_SIZE);
                long indexCacheRetainTime = buffer.readLong();
                long indexCommitTime = buffer.readLong();
                IndexAndMetadata indexAndMetadata = new IndexAndMetadata(index, metadata);
                partitionMetadata.put(partition, indexAndMetadata);
            }
            indexMetadata.put(topic, partitionMetadata);
        }
        return new ConsumeIndexStoreRequest(app, indexMetadata);
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_STORE_REQUEST;
    }
}
