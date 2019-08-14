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
package io.chubao.joyqueue.broker.election.network.codec;

import io.chubao.joyqueue.broker.election.TopicPartitionGroup;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadDecoder;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.broker.election.command.TimeoutNowRequest;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/10/01
 */
public class TimeoutNowRequestDecoder implements PayloadDecoder<JoyQueueHeader>, Type {
    @Override
    public Object decode(final JoyQueueHeader header, final ByteBuf buffer) throws Exception {
        String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
        int partitionGroup = buffer.readInt();
        int term = buffer.readInt();
        return new TimeoutNowRequest(new TopicPartitionGroup(topic, partitionGroup), term);
    }

    @Override
    public int type() {
        return CommandType.RAFT_TIMEOUT_NOW_REQUEST;
    }
}
