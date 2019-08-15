package io.chubao.joyqueue.broker.election.network.codec;

import io.chubao.joyqueue.broker.election.TopicPartitionGroup;
import io.chubao.joyqueue.broker.election.command.VoteRequest;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadDecoder;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Type;
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
