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
package org.joyqueue.broker.election.network.codec;

import org.joyqueue.broker.election.TopicPartitionGroup;
import org.joyqueue.broker.election.command.VoteRequest;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadDecoder;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/20
 */
public class VoteRequestDecoder implements PayloadDecoder<JoyQueueHeader>, Type {
    @Override
    public VoteRequest decode(JoyQueueHeader header, final ByteBuf buffer) throws Exception {
        VoteRequest voteRequest = new VoteRequest();

        String topic = Serializer.readString(buffer);
        int partitionGroupId = buffer.readInt();
        voteRequest.setTopicPartitionGroup(new TopicPartitionGroup(topic, partitionGroupId));
        voteRequest.setTerm(buffer.readInt());
        voteRequest.setCandidateId(buffer.readInt());
        voteRequest.setLastLogTerm(buffer.readInt());
        voteRequest.setLastLogPos(buffer.readLong());
        voteRequest.setPreVote(buffer.readBoolean());

        return voteRequest;
    }

    @Override
    public int type() {
        return CommandType.RAFT_VOTE_REQUEST;
    }
}
