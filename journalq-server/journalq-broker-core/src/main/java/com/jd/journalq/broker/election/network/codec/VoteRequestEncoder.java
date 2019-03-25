package com.jd.journalq.broker.election.network.codec;

import com.jd.journalq.broker.election.command.VoteRequest;
import com.jd.journalq.common.network.transport.codec.PayloadEncoder;
import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Type;
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
