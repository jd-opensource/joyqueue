package com.jd.journalq.network.codec;

import com.google.common.collect.Maps;
import com.jd.journalq.network.command.FetchTopicMessageRequest;
import com.jd.journalq.network.command.FetchTopicMessageData;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * FetchTopicMessageCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/7
 */
public class FetchTopicMessageCodec implements PayloadCodec<JMQHeader, FetchTopicMessageRequest>, Type {

    @Override
    public FetchTopicMessageRequest decode(JMQHeader header, ByteBuf buffer) throws Exception {
        FetchTopicMessageRequest fetchTopicMessageRequest = new FetchTopicMessageRequest();

        short topicSize = buffer.readShort();
        Map<String, FetchTopicMessageData> topics = Maps.newHashMapWithExpectedSize(topicSize);
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            short count = buffer.readShort();
            FetchTopicMessageData fetchTopicMessageData = new FetchTopicMessageData();
            fetchTopicMessageData.setCount(count);
            topics.put(topic, fetchTopicMessageData);
        }

        fetchTopicMessageRequest.setTopics(topics);
        fetchTopicMessageRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        fetchTopicMessageRequest.setAckTimeout(buffer.readInt());
        fetchTopicMessageRequest.setLongPollTimeout(buffer.readInt());
        return fetchTopicMessageRequest;
    }

    @Override
    public void encode(FetchTopicMessageRequest payload, ByteBuf buffer) throws Exception {
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