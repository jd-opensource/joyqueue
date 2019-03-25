package com.jd.journalq.broker.election.handler;

import com.jd.journalq.broker.election.command.TimeoutNowRequest;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.election.ElectionManager;
import com.jd.journalq.broker.election.ElectionService;
import com.jd.journalq.broker.election.RaftLeaderElection;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.common.network.transport.command.handler.CommandHandler;
import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.exception.TransportException;
import com.jd.journalq.toolkit.lang.Preconditions;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/10/2
 */
public class TimeoutNowRequestHandler implements CommandHandler, Type {
    private ElectionManager electionManager;

    public TimeoutNowRequestHandler(BrokerContext brokerContext) {
        Preconditions.checkArgument(brokerContext != null, "broker context is null");
        Preconditions.checkArgument(brokerContext.getElectionService() != null, "election manager is null");

        electionManager = (ElectionManager)brokerContext.getElectionService();
    }

    public TimeoutNowRequestHandler(ElectionService electionService) {
        electionManager = (ElectionManager)electionService;
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        TimeoutNowRequest request = (TimeoutNowRequest)command.getPayload();

        RaftLeaderElection leaderElection = (RaftLeaderElection)electionManager.getLeaderElection(request.getTopic(),
                request.getPartitionGroup());
        return leaderElection.handleTimeoutNowRequest(request);

    }

    @Override
    public int type() {
        return CommandType.RAFT_TIMEOUT_NOW_REQUEST;
    }
}
