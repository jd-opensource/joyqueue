package com.jd.journalq.broker.index.network.codec;

import com.jd.journalq.broker.index.command.ConsumeIndexQueryRequest;
import com.jd.journalq.common.network.transport.codec.PayloadEncoder;
import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;
import java.util.Set;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class IndexQueryRequestEncoder implements PayloadEncoder<ConsumeIndexQueryRequest>, Type {

    @Override
    public void encode(ConsumeIndexQueryRequest payload, ByteBuf buf) throws Exception {
        Map<String, Set<Integer>> indexTopicPartitions = payload.getTopicPartitions();
        Serializer.write(payload.getApp(), buf, Serializer.SHORT_SIZE);
        buf.writeInt(indexTopicPartitions.size());
        for (String topic : indexTopicPartitions.keySet()) {
            Serializer.write(topic, buf, Serializer.SHORT_SIZE);
            Set<Integer> partitions = indexTopicPartitions.get(topic);
            buf.writeInt(partitions.size());
            for (int partition : partitions) {
                buf.writeInt(partition);
            }
        }
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_QUERY_REQUEST;
    }
}
