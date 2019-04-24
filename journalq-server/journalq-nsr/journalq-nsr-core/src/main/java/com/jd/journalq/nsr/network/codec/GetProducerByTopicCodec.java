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
package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.domain.TopicName;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetProducerByTopic;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetProducerByTopicCodec implements NsrPayloadCodec<GetProducerByTopic>, Type {
    @Override
    public GetProducerByTopic decode(Header header, ByteBuf buffer) throws Exception {
        return new GetProducerByTopic().topic(TopicName.parse(Serializer.readString(buffer)));
    }

    @Override
    public void encode(GetProducerByTopic payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic().getFullName(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_PRODUCER_BY_TOPIC;
    }
}
