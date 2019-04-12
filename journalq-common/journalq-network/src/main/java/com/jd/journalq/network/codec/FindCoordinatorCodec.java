package com.jd.journalq.network.codec;

import com.google.common.collect.Lists;
import com.jd.journalq.network.command.FindCoordinatorRequest;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * FindCoordinatorCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class FindCoordinatorCodec implements PayloadCodec<JMQHeader, FindCoordinatorRequest>, Type {

    @Override
    public FindCoordinatorRequest decode(JMQHeader header, ByteBuf buffer) throws Exception {
        FindCoordinatorRequest findCoordinatorRequest = new FindCoordinatorRequest();

        short topicSize = buffer.readShort();
        List<String> topics = Lists.newArrayListWithCapacity(topicSize);
        for (int i = 0; i < topicSize; i++) {
            topics.add(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        }

        findCoordinatorRequest.setTopics(topics);
        findCoordinatorRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return findCoordinatorRequest;
    }

    @Override
    public void encode(FindCoordinatorRequest payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getTopics().size());
        for (String topic : payload.getTopics()) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JMQCommandType.FIND_COORDINATOR_REQUEST.getCode();
    }
}