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
package com.jd.joyqueue.broker.election.network.codec;

import com.jd.joyqueue.broker.election.TopicPartitionGroup;
import com.jd.joyqueue.broker.election.command.AppendEntriesRequest;
import com.jd.joyqueue.network.command.CommandType;
import com.jd.joyqueue.network.serializer.Serializer;
import com.jd.joyqueue.network.transport.codec.JoyQueueHeader;
import com.jd.joyqueue.network.transport.codec.PayloadDecoder;
import com.jd.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/27
 */
public class AppendEntriesRequestDecoder implements PayloadDecoder<JoyQueueHeader>, Type {
    @Override
    public Object decode(final JoyQueueHeader header, final ByteBuf buffer) throws Exception {

        AppendEntriesRequest request = new AppendEntriesRequest();

        String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
        int partitionGroupId = buffer.readInt();
        request.setTopicPartitionGroup(new TopicPartitionGroup(topic, partitionGroupId));
        request.setTerm(buffer.readInt());
        request.setLeaderId(buffer.readInt());

        request.setPrevTerm(buffer.readInt());
        request.setPrevPosition(buffer.readLong());

        request.setStartPosition(buffer.readLong());
        request.setCommitPosition(buffer.readLong());
        request.setLeftPosition(buffer.readLong());

        request.setMatch(buffer.readBoolean());

        int length = buffer.readInt();
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes, 0, length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.rewind();

        /*
        int length = buffer.readInt();
        ByteBuffer byteBuffer = buffer.nioBuffer(buffer.readerIndex(), length);
        buffer.readerIndex(buffer.readerIndex() + length);
        */

        request.setEntries(byteBuffer);
        return request;

    }

    @Override
    public int type() {
        return CommandType.RAFT_APPEND_ENTRIES_REQUEST;
    }
}
