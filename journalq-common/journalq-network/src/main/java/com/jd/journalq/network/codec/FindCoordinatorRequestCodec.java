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
package com.jd.journalq.network.codec;

import com.google.common.collect.Lists;
import com.jd.journalq.network.command.FindCoordinatorRequest;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JournalqHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * FindCoordinatorRequestCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class FindCoordinatorRequestCodec implements PayloadCodec<JournalqHeader, FindCoordinatorRequest>, Type {

    @Override
    public FindCoordinatorRequest decode(JournalqHeader header, ByteBuf buffer) throws Exception {
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
        return JournalqCommandType.FIND_COORDINATOR_REQUEST.getCode();
    }
}