package com.jd.journalq.broker.index.network.codec;

import com.jd.journalq.broker.index.command.ConsumeIndexStoreRequest;
import com.jd.journalq.broker.index.model.IndexAndMetadata;
import com.jd.journalq.common.network.transport.codec.PayloadEncoder;
import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class IndexStoreRequestEncoder implements PayloadEncoder<ConsumeIndexStoreRequest>, Type {

    @Override
    public void encode(final ConsumeIndexStoreRequest request, ByteBuf buffer) throws Exception {
        Map<String, Map<Integer, IndexAndMetadata>> indexMetadata = request.getIndexMetadata();
        Serializer.write(request.getApp(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(indexMetadata.size());
        for (String topic : indexMetadata.keySet()) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
            Map<Integer, IndexAndMetadata> partitionMetadata = indexMetadata.get(topic);
            buffer.writeInt(partitionMetadata.size());
            for (int partition : partitionMetadata.keySet()) {
                buffer.writeInt(partition);
                IndexAndMetadata indexAndMetadata = partitionMetadata.get(partition);
                buffer.writeLong(indexAndMetadata.getIndex());
                Serializer.write(indexAndMetadata.getMetadata(), buffer, Serializer.SHORT_SIZE);
                buffer.writeLong(indexAndMetadata.getIndexCacheRetainTime());
                buffer.writeLong(indexAndMetadata.getIndexCommitTime());
            }
        }
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_STORE_REQUEST;
    }
}
