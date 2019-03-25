package com.jd.journalq.broker.election.handler;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.election.ElectionManager;
import com.jd.journalq.broker.election.ElectionService;
import com.jd.journalq.broker.election.RaftLeaderElection;
import com.jd.journalq.broker.election.command.VoteRequest;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.network.transport.command.handler.CommandHandler;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.exception.TransportException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/15
 */
public class VoteRequestHandler implements CommandHandler, Type {
    private static Logger logger = LoggerFactory.getLogger(VoteRequestHandler.class);
    private ElectionManager electionManager;

    public VoteRequestHandler(BrokerContext brokerContext) {
        this.electionManager = (ElectionManager)brokerContext.getElectionService();
    }

    public VoteRequestHandler(ElectionService electionService) {
        this.electionManager = (ElectionManager)electionService;
    }

    @Override
    public int type() {
        return CommandType.RAFT_VOTE_REQUEST;
    }

    public Command handle(Transport transport, Command command) throws TransportException{
        if (command == null) {
            logger.error("Receive vote request command is null");
            return null;
        }

        VoteRequest voteRequest = (VoteRequest)command.getPayload();
        RaftLeaderElection election = (RaftLeaderElection) electionManager.getLeaderElection(
                voteRequest.getTopic(), voteRequest.getPartitionGroup());
        if (election == null) {
            logger.warn("Receive vote request of {}, election is null", voteRequest.getTopicPartitionGroup());
            return null;
        }
        if (voteRequest.isPreVote()) {
            return election.handlePreVoteRequest(voteRequest);
        } else {
            return election.handleVoteRequest(voteRequest);
        }
    }

}
