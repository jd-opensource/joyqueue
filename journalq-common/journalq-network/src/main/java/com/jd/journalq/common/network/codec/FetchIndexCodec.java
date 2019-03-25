package com.jd.journalq.common.network.codec;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.common.network.command.FetchIndex;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * FetchIndexCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchIndexCodec implements PayloadCodec<JMQHeader, FetchIndex>, Type {

    @Override
    public FetchIndex decode(JMQHeader header, ByteBuf buffer) throws Exception {
        Map<String, List<Short>> result = Maps.newHashMap();
        short topicSize = buffer.readShort();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            short partitionSize = buffer.readShort();
            List<Short> partitions = Lists.newLinkedList();
            for (int j = 0; j < partitionSize; j++) {
                partitions.add(buffer.readShort());
            }
            result.put(topic, partitions);
        }

        FetchIndex fetchIndex = new FetchIndex();
        fetchIndex.setPartitions(result);
        fetchIndex.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return fetchIndex;
    }

    @Override
    public void encode(FetchIndex payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getPartitions().size());
        for (Map.Entry<String, List<Short>> entry : payload.getPartitions().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeShort(entry.getValue().size());
            for (Short partition : entry.getValue()) {
                buffer.writeShort(partition);
            }
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_INDEX.getCode();
    }
}