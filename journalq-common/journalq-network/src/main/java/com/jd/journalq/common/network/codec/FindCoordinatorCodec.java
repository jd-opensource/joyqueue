package com.jd.journalq.common.network.codec;

import com.google.common.collect.Lists;
import com.jd.journalq.common.network.command.FindCoordinator;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * FindCoordinatorCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class FindCoordinatorCodec implements PayloadCodec<JMQHeader, FindCoordinator>, Type {

    @Override
    public FindCoordinator decode(JMQHeader header, ByteBuf buffer) throws Exception {
        FindCoordinator findCoordinator = new FindCoordinator();

        short topicSize = buffer.readShort();
        List<String> topics = Lists.newArrayListWithCapacity(topicSize);
        for (int i = 0; i < topicSize; i++) {
            topics.add(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        }

        findCoordinator.setTopics(topics);
        findCoordinator.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return findCoordinator;
    }

    @Override
    public void encode(FindCoordinator payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getTopics().size());
        for (String topic : payload.getTopics()) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JMQCommandType.FIND_COORDINATOR.getCode();
    }
}