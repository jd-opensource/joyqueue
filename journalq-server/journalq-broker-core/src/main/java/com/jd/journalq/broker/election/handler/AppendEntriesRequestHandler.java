package com.jd.journalq.broker.election.handler;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.election.ElectionManager;
import com.jd.journalq.broker.election.ElectionService;
import com.jd.journalq.broker.election.LeaderElection;
import com.jd.journalq.broker.election.command.AppendEntriesRequest;
import com.jd.journalq.broker.election.command.AppendEntriesResponse;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.transport.command.Direction;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.common.network.transport.command.handler.CommandHandler;
import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.exception.TransportException;
import com.jd.journalq.toolkit.lang.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/27
 */
public class AppendEntriesRequestHandler implements CommandHandler, Type {
    private static Logger logger = LoggerFactory.getLogger(AppendEntriesRequestHandler.class);

    private ElectionManager electionManager;

    public AppendEntriesRequestHandler(BrokerContext brokerContext) {
        Preconditions.checkArgument(brokerContext != null, "broker context is null");
        Preconditions.checkArgument(brokerContext.getElectionService() != null, "election service is null");

        this.electionManager = (ElectionManager)brokerContext.getElectionService();
    }

    public AppendEntriesRequestHandler(ElectionService electionService) {
        this.electionManager = (ElectionManager)electionService;
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        logger.debug("Receive append entries request from {}", transport.remoteAddress());

        AppendEntriesRequest request = (AppendEntriesRequest) command.getPayload();
        if (request == null) {
            logger.warn("Receive append entries request from {}, request is null", transport.remoteAddress());
            throw new TransportException("Append entries request payload is null",
                    JMQCode.CT_MESSAGE_BODY_NULL.getCode());
        }

        try {
            LeaderElection leaderElection = electionManager.getLeaderElection(request.getTopic(),
                    request.getPartitionGroup());
            if (leaderElection == null) {
                logger.warn("Handle append entries request of topic {} partition group {} election is null",
                        request.getTopic(), request.getPartitionGroup());
                return new Command(new JMQHeader(Direction.RESPONSE, CommandType.RAFT_APPEND_ENTRIES_RESPONSE),
                        new AppendEntriesResponse.Build().success(false).nextPosition(-1L).build());
            }

            return leaderElection.handleAppendEntriesRequest(request);

        } catch (Exception e) {
            logger.warn("Handle append entries request of topic {} partition group {} fail",
                    request.getTopic(), request.getPartitionGroup(), e);
            return new Command(new JMQHeader(Direction.RESPONSE, CommandType.RAFT_APPEND_ENTRIES_RESPONSE),
                               new AppendEntriesResponse.Build().success(false).nextPosition(-1L).build());
        }

    }

    @Override
    public int type() {
        return CommandType.RAFT_APPEND_ENTRIES_REQUEST;
    }
}
