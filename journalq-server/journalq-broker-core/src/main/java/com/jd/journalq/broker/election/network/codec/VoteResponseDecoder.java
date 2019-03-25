package com.jd.journalq.broker.election.network.codec;

import com.jd.journalq.broker.election.command.VoteResponse;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadDecoder;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/20
 */
public class VoteResponseDecoder implements PayloadDecoder<JMQHeader>, Type {
    @Override
    public VoteResponse decode(JMQHeader header, final ByteBuf buffer) {
        VoteResponse voteResponse = new VoteResponse();

        voteResponse.setTerm(buffer.readInt());
        voteResponse.setCandidateId(buffer.readInt());
        voteResponse.setVoteNodeId(buffer.readInt());
        voteResponse.setVoteGranted(buffer.readBoolean());

        return voteResponse;
    }

    @Override
    public int type() {
        return CommandType.RAFT_VOTE_RESPONSE;
    }
}
