package com.jd.journalq.broker.index.network.codec;

import com.jd.journalq.broker.index.command.ConsumeIndexStoreResponse;
import com.jd.journalq.network.transport.codec.PayloadEncoder;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class IndexStoreResponseEncoder implements PayloadEncoder<ConsumeIndexStoreResponse>, Type {

    @Override
    public void encode(final ConsumeIndexStoreResponse response, final ByteBuf buffer) throws Exception {
        Map<String, Map<Integer, Short>> indexStoreStatus = response.getIndexStoreStatus();
        buffer.writeInt(indexStoreStatus.size());
        for (String topic : indexStoreStatus.keySet()) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
            Map<Integer, Short> partitionStoreStatus = indexStoreStatus.get(topic);
            buffer.writeInt(partitionStoreStatus.size());
            for (int partition : partitionStoreStatus.keySet()) {
                buffer.writeInt(partition);
                buffer.writeShort(partitionStoreStatus.get(partition));
            }
        }
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_STORE_RESPONSE;
    }
}
