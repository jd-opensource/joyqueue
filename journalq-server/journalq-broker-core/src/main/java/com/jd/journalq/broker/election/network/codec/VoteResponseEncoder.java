package com.jd.journalq.broker.election.network.codec;

import com.jd.journalq.broker.election.command.VoteResponse;
import com.jd.journalq.common.network.transport.codec.PayloadEncoder;
import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/20
 */
public class VoteResponseEncoder implements PayloadEncoder<VoteResponse>, Type {
    @Override
    public void encode(final VoteResponse voteResponse, ByteBuf buffer) {
        buffer.writeInt(voteResponse.getTerm());
        buffer.writeInt(voteResponse.getCandidateId());
        buffer.writeInt(voteResponse.getVoteNodeId());
        buffer.writeBoolean(voteResponse.isVoteGranted());
    }

    @Override
    public int type() {
        return CommandType.RAFT_VOTE_RESPONSE;
    }
}
