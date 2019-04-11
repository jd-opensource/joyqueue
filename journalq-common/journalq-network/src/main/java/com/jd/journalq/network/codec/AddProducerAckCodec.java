package com.jd.journalq.network.codec;

import com.google.common.collect.Maps;
import com.jd.journalq.network.command.AddProducerResponse;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * AddProducerResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public class AddProducerAckCodec implements PayloadCodec<JMQHeader, AddProducerResponse>, Type {

    @Override
    public AddProducerResponse decode(JMQHeader header, ByteBuf buffer) throws Exception {
        Map<String, String> result = Maps.newHashMap();
        short producerSize = buffer.readShort();
        for (int i = 0; i < producerSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            String producerId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            result.put(topic, producerId);
        }

        AddProducerResponse addProducerResponse = new AddProducerResponse();
        addProducerResponse.setProducerIds(result);
        return addProducerResponse;
    }

    @Override
    public void encode(AddProducerResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getProducerIds().size());
        for (Map.Entry<String, String> entry : payload.getProducerIds().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            Serializer.write(entry.getValue(), buffer, Serializer.SHORT_SIZE);
        }
    }

    @Override
    public int type() {
        return JMQCommandType.ADD_PRODUCER_ACK.getCode();
    }
}