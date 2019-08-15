package io.chubao.joyqueue.broker.election.network.codec;

import io.chubao.joyqueue.broker.election.command.VoteResponse;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadDecoder;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/20
 */
public class VoteResponseDecoder implements PayloadDecoder<JoyQueueHeader>, Type {
    @Override
    public VoteResponse decode(JoyQueueHeader header, final ByteBuf buffer) {
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
