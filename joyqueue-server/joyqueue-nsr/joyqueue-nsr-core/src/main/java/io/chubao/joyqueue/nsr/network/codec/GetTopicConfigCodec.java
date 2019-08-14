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
package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetTopicConfig;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetTopicConfigCodec implements NsrPayloadCodec<GetTopicConfig>, Type {
    @Override
    public GetTopicConfig decode(Header header, ByteBuf buffer) throws Exception {
        return new GetTopicConfig().topic(TopicName.parse(Serializer.readString(buffer)));
    }

    @Override
    public void encode(GetTopicConfig payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic().getFullName(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_TOPICCONFIG;
    }
}
