package io.chubao.joyqueue.broker.index.network.codec;

import io.chubao.joyqueue.broker.index.command.ConsumeIndexStoreRequest;
import io.chubao.joyqueue.broker.index.model.IndexAndMetadata;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadDecoder;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Type;

import io.netty.buffer.ByteBuf;
import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class IndexStoreRequestDecoder implements PayloadDecoder<JoyQueueHeader>, Type {

    @Override
    public Object decode(final JoyQueueHeader header, final ByteBuf buffer) throws Exception {
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
