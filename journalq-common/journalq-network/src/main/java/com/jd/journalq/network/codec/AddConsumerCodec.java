package com.jd.journalq.network.codec;

import com.google.common.collect.Lists;
import com.jd.journalq.network.command.AddConsumer;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;


/**
 * AddConsumerCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public class AddConsumerCodec implements PayloadCodec<JMQHeader, AddConsumer>, Type {

    @Override
    public AddConsumer decode(JMQHeader header, ByteBuf buffer) throws Exception {
        AddConsumer addConsumer = new AddConsumer();

        short topicSize = buffer.readShort();
        List<String> topics = Lists.newArrayListWithCapacity(topicSize);
        for (int i = 0; i < topicSize; i++) {
            topics.add(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        }

        addConsumer.setTopics(topics);
        addConsumer.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        addConsumer.setSequence(buffer.readLong());
        return addConsumer;
    }

    @Override
    public void encode(AddConsumer payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getTopics().size());
        for (String topic : payload.getTopics()) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
        buffer.writeLong(payload.getSequence());
    }

    @Override
    public int type() {
        return JMQCommandType.ADD_CONSUMER.getCode();
    }
}