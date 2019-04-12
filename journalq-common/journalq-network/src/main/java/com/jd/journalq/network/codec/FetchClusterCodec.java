package com.jd.journalq.network.codec;

import com.google.common.collect.Lists;
import com.jd.journalq.network.command.FetchClusterRequest;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * FetchClusterCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/30
 */
public class FetchClusterCodec implements PayloadCodec<JMQHeader, FetchClusterRequest>, Type {

    @Override
    public Object decode(JMQHeader header, ByteBuf buffer) throws Exception {
        FetchClusterRequest fetchClusterRequest = new FetchClusterRequest();

        short topicSize = buffer.readShort();
        List<String> topics = Lists.newArrayListWithCapacity(topicSize);
        for (int i = 0; i < topicSize; i++) {
            topics.add(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        }

        fetchClusterRequest.setTopics(topics);
        fetchClusterRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return fetchClusterRequest;
    }

    @Override
    public void encode(FetchClusterRequest payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getTopics().size());
        for (String topic : payload.getTopics()) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_CLUSTER_REQUEST.getCode();
    }
}