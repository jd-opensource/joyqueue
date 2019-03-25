package com.jd.journalq.common.network.codec;

import com.google.common.collect.Maps;
import com.jd.journalq.common.network.command.FetchTopicMessage;
import com.jd.journalq.common.network.command.FetchTopicMessageData;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * FetchTopicMessageCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/7
 */
public class FetchTopicMessageCodec implements PayloadCodec<JMQHeader, FetchTopicMessage>, Type {

    @Override
    public FetchTopicMessage decode(JMQHeader header, ByteBuf buffer) throws Exception {
        FetchTopicMessage fetchTopicMessage = new FetchTopicMessage();

        Map<String, FetchTopicMessageData> topics = Maps.newHashMap();
        short topicSize = buffer.readShort();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            short count = buffer.readShort();
            FetchTopicMessageData fetchTopicMessageData = new FetchTopicMessageData();
            fetchTopicMessageData.setCount(count);
            topics.put(topic, fetchTopicMessageData);
        }

        fetchTopicMessage.setTopics(topics);
        fetchTopicMessage.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        fetchTopicMessage.setAckTimeout(buffer.readInt());
        fetchTopicMessage.setLongPollTimeout(buffer.readInt());
        return fetchTopicMessage;
    }

    @Override
    public void encode(FetchTopicMessage payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getTopics().size());
        for (Map.Entry<String, FetchTopicMessageData> entry : payload.getTopics().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeShort(entry.getValue().getCount());
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(payload.getAckTimeout());
        buffer.writeInt(payload.getLongPollTimeout());
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_TOPIC_MESSAGE.getCode();
    }
}