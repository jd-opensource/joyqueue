package com.jd.journalq.network.codec;

import com.google.common.collect.Lists;
import com.jd.journalq.network.command.AddProducerRequest;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * AddProducerCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public class AddProducerCodec implements PayloadCodec<JMQHeader, AddProducerRequest>, Type {

    @Override
    public AddProducerRequest decode(JMQHeader header, ByteBuf buffer) throws Exception {
        AddProducerRequest addProducerRequest = new AddProducerRequest();

        short topicSize = buffer.readShort();
        List<String> topics = Lists.newArrayListWithCapacity(topicSize);
        for (int i = 0; i < topicSize; i++) {
            topics.add(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        }

        addProducerRequest.setTopics(topics);
        addProducerRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        addProducerRequest.setSequence(buffer.readLong());
        return addProducerRequest;
    }

    @Override
    public void encode(AddProducerRequest payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getTopics().size());
        for (String topic : payload.getTopics()) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
        buffer.writeLong(payload.getSequence());
    }

    @Override
    public int type() {
        return JMQCommandType.ADD_PRODUCER_REQUEST.getCode();
    }
}