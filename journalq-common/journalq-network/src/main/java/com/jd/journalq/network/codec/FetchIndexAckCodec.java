package com.jd.journalq.network.codec;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.command.FetchIndexResponse;
import com.jd.journalq.network.command.FetchIndexAckData;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * FetchIndexAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchIndexAckCodec implements PayloadCodec<JMQHeader, FetchIndexResponse>, Type {

    @Override
    public FetchIndexResponse decode(JMQHeader header, ByteBuf buffer) throws Exception {
        Table<String, Short, FetchIndexAckData> result = HashBasedTable.create();
        short topicSize = buffer.readShort();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            short partitionSize = buffer.readShort();
            for (int j = 0; j < partitionSize; j++) {
                short partition = buffer.readShort();
                long index = buffer.readLong();
                JMQCode code = JMQCode.valueOf(buffer.readInt());
                FetchIndexAckData fetchIndexAckData = new FetchIndexAckData(index, code);
                result.put(topic, partition, fetchIndexAckData);
            }
        }

        FetchIndexResponse fetchIndexResponse = new FetchIndexResponse();
        fetchIndexResponse.setData(result);
        return fetchIndexResponse;
    }

    @Override
    public void encode(FetchIndexResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getData().rowMap().size());
        for (Map.Entry<String, Map<Short, FetchIndexAckData>> topicEntry : payload.getData().rowMap().entrySet()) {
            Serializer.write(topicEntry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeShort(topicEntry.getValue().size());
            for (Map.Entry<Short, FetchIndexAckData> partitionEntry : topicEntry.getValue().entrySet()) {
                FetchIndexAckData fetchIndexAckData = partitionEntry.getValue();
                buffer.writeShort(partitionEntry.getKey());
                buffer.writeLong(fetchIndexAckData.getIndex());
                buffer.writeInt(fetchIndexAckData.getCode().getCode());
            }
        }
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_INDEX_RESPONSE.getCode();
    }
}