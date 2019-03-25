package com.jd.journalq.common.network.codec;

import com.google.common.collect.Lists;
import com.jd.journalq.common.network.command.FetchCluster;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * FetchClusterCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/30
 */
public class FetchClusterCodec implements PayloadCodec<JMQHeader, FetchCluster>, Type {

    @Override
    public Object decode(JMQHeader header, ByteBuf buffer) throws Exception {
        FetchCluster fetchCluster = new FetchCluster();

        short topicSize = buffer.readShort();
        List<String> topics = Lists.newArrayListWithCapacity(topicSize);
        for (int i = 0; i < topicSize; i++) {
            topics.add(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        }

        fetchCluster.setTopics(topics);
        fetchCluster.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return fetchCluster;
    }

    @Override
    public void encode(FetchCluster payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getTopics().size());
        for (String topic : payload.getTopics()) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_CLUSTER.getCode();
    }
}