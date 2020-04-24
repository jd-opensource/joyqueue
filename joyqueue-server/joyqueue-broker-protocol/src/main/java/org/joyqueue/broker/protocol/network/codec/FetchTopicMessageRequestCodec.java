package org.joyqueue.broker.protocol.network.codec;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import org.joyqueue.broker.protocol.command.FetchTopicMessageRequest;
import org.joyqueue.network.command.FetchTopicMessageData;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;

import java.util.Map;

/**
 * FetchTopicMessageRequestCodec
 * author: gaohaoxiang
 * date: 2020/4/7
 */
public class FetchTopicMessageRequestCodec extends org.joyqueue.network.codec.FetchTopicMessageRequestCodec implements JoyQueuePayloadCodec<org.joyqueue.network.command.FetchTopicMessageRequest> {

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
}