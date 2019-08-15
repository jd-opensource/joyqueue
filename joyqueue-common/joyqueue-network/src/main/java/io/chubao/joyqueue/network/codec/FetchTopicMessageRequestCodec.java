package io.chubao.joyqueue.network.codec;

import com.google.common.collect.Maps;
import io.chubao.joyqueue.network.command.FetchTopicMessageRequest;
import io.chubao.joyqueue.network.command.FetchTopicMessageData;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * FetchTopicMessageRequestCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/7
 */
public class FetchTopicMessageRequestCodec implements PayloadCodec<JoyQueueHeader, FetchTopicMessageRequest>, Type {

    @Override
    public FetchTopicMessageRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
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
        return JoyQueueCommandType.FETCH_TOPIC_MESSAGE_REQUEST.getCode();
    }
}