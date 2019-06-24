/**
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
package com.jd.joyqueue.network.codec;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.joyqueue.exception.JournalqCode;
import com.jd.joyqueue.message.BrokerMessage;
import com.jd.joyqueue.network.command.FetchTopicMessageResponse;
import com.jd.joyqueue.network.command.FetchTopicMessageAckData;
import com.jd.joyqueue.network.command.JournalqCommandType;
import com.jd.joyqueue.network.serializer.Serializer;
import com.jd.joyqueue.network.transport.codec.JournalqHeader;
import com.jd.joyqueue.network.transport.codec.PayloadCodec;
import com.jd.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * FetchTopicMessageResponseCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/7
 */
public class FetchTopicMessageResponseCodec implements PayloadCodec<JournalqHeader, FetchTopicMessageResponse>, Type {

    @Override
    public FetchTopicMessageResponse decode(JournalqHeader header, ByteBuf buffer) throws Exception {
        Map<String, FetchTopicMessageAckData> result = Maps.newHashMap();
        short topicSize = buffer.readShort();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            short messageSize = buffer.readShort();
            List<BrokerMessage> messages = Lists.newArrayListWithCapacity(messageSize);
            for (int j = 0; j < messageSize; j++) {
                messages.add(Serializer.readBrokerMessage(buffer));
            }
            JournalqCode code = JournalqCode.valueOf(buffer.readInt());
            FetchTopicMessageAckData fetchTopicMessageAckData = new FetchTopicMessageAckData(messages, code);
            result.put(topic, fetchTopicMessageAckData);
        }

        FetchTopicMessageResponse fetchTopicMessageResponse = new FetchTopicMessageResponse();
        fetchTopicMessageResponse.setData(result);
        return fetchTopicMessageResponse;
    }

    @Override
    public void encode(FetchTopicMessageResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getData().size());
        for (Map.Entry<String, FetchTopicMessageAckData> entry : payload.getData().entrySet()) {
            FetchTopicMessageAckData fetchTopicMessageAckData = entry.getValue();
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeShort(fetchTopicMessageAckData.getBuffers().size());
            for (ByteBuffer rByteBuffer : fetchTopicMessageAckData.getBuffers()) {
                buffer.writeBytes(rByteBuffer);
            }
            buffer.writeInt(fetchTopicMessageAckData.getCode().getCode());
        }
    }

    @Override
    public int type() {
        return JournalqCommandType.FETCH_TOPIC_MESSAGE_RESPONSE.getCode();
    }
}
