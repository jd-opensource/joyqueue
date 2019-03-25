package com.jd.journalq.broker.election.command;


import com.jd.journalq.common.network.transport.command.JMQPayload;
import com.jd.journalq.common.network.command.CommandType;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/15
 */
public class VoteResponse extends JMQPayload {
    private int term;
    private int candidateId;
    private int voteNodeId;
    private boolean voteGranted;

    public VoteResponse(){}

    public VoteResponse(int term, int candidateId, int voteNodeId, boolean voteGranted) {
        this.term = term;
        this.candidateId = candidateId;
        this.voteNodeId = voteNodeId;
        this.voteGranted = voteGranted;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public int getVoteNodeId() {
        return voteNodeId;
    }

    public void setVoteNodeId(int voteNodeId) {
        this.voteNodeId = voteNodeId;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

    public void setVoteGranted(boolean voteGranted) {
        this.voteGranted = voteGranted;
    }

    @Override
    public int type() {
        return CommandType.RAFT_VOTE_RESPONSE;
    }
}
