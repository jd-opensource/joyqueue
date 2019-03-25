package com.jd.journalq.broker.election.network.codec;

import com.jd.journalq.broker.election.TopicPartitionGroup;
import com.jd.journalq.broker.election.command.VoteRequest;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadDecoder;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/20
 */
public class VoteRequestDecoder implements PayloadDecoder<JMQHeader>, Type {
    @Override
    public VoteRequest decode(JMQHeader header, final ByteBuf buffer) throws Exception {
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
