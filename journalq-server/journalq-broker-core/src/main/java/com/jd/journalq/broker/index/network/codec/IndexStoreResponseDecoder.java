package com.jd.journalq.broker.index.network.codec;

import com.jd.journalq.broker.index.command.ConsumeIndexStoreResponse;
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
public class IndexStoreResponseDecoder implements PayloadDecoder<JMQHeader>, Type {

    @Override
    public Object decode(final JMQHeader header, final ByteBuf buffer) throws Exception {
        Map<String, Map<Integer, Short>> indexStoreStatus = new HashedMap();
        int topics = buffer.readInt();
        for (int i = 0; i < topics; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int partitions = buffer.readInt();
            Map<Integer, Short> partitionStoreStatus = new HashedMap();
            for (int j = 0; j < partitions; j++) {
                int partition = buffer.readInt();
                short code = buffer.readShort();
                partitionStoreStatus.put(partition, code);
            }
            indexStoreStatus.put(topic, partitionStoreStatus);
        }
        return new ConsumeIndexStoreResponse(indexStoreStatus);
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_STORE_RESPONSE;
    }
}
