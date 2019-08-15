package io.chubao.joyqueue.broker.election.network.codec;

import io.chubao.joyqueue.broker.election.command.VoteRequest;
import io.chubao.joyqueue.network.transport.codec.PayloadEncoder;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/20
 */
public class VoteRequestEncoder implements PayloadEncoder<VoteRequest>, Type {
    @Override
    public void encode(final VoteRequest voteRequest, ByteBuf buffer) throws Exception {
        Serializer.write(voteRequest.getTopicPartitionGroup().getTopic(), buffer);
        buffer.writeInt(voteRequest.getTopicPartitionGroup().getPartitionGroupId());
        buffer.writeInt(voteRequest.getTerm());
        buffer.writeInt(voteRequest.getCandidateId());
        buffer.writeInt(voteRequest.getLastLogTerm());
        buffer.writeLong(voteRequest.getLastLogPos());
        buffer.writeBoolean(voteRequest.isPreVote());
    }

    @Override
    public int type() {
        return CommandType.RAFT_VOTE_REQUEST;
    }
}
