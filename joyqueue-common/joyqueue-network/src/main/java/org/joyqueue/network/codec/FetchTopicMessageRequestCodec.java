/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.network.codec;

import com.google.common.collect.Maps;
import org.joyqueue.network.command.FetchTopicMessageRequest;
import org.joyqueue.network.command.FetchTopicMessageData;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * FetchTopicMessageRequestCodec
 *
 * author: gaohaoxiang
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