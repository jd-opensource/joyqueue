/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.election.handler;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.election.ElectionManager;
import org.joyqueue.broker.election.ElectionService;
import org.joyqueue.broker.election.LeaderElection;
import org.joyqueue.broker.election.command.AppendEntriesRequest;
import org.joyqueue.broker.election.command.AppendEntriesResponse;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.network.transport.command.handler.CommandHandler;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.exception.TransportException;
import com.google.common.base.Preconditions;

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

        if (!(brokerContext.getElectionService() instanceof ElectionManager)) {
            logger.info("Append entries request handler, election service {} not election manager",
                    brokerContext.getElectionService());
            throw new IllegalArgumentException();
        }
        this.electionManager = (ElectionManager)brokerContext.getElectionService();
    }

    public AppendEntriesRequestHandler(ElectionService electionService) {
        if (!(electionService instanceof ElectionManager)) {
            logger.info("Append entries request handler, election service {} not election manager",
                    electionService);
            throw new IllegalArgumentException();
        }
        this.electionManager = (ElectionManager)electionService;
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        if (!(command.getPayload() instanceof AppendEntriesRequest)) {
            throw new IllegalArgumentException();
        }

        AppendEntriesRequest request = (AppendEntriesRequest) command.getPayload();
        if (request == null) {
            logger.warn("Receive append entries request from {}, request is null", transport.remoteAddress());
            throw new TransportException("Append entries request payload is null",
                    JoyQueueCode.CT_MESSAGE_BODY_NULL.getCode());
        }

        if(logger.isDebugEnabled()){
            logger.debug("Receive append entries request {} from {}", request, transport.remoteAddress());
        }

        try {
            LeaderElection leaderElection = electionManager.getLeaderElection(request.getTopic(),
                    request.getPartitionGroup());
            if (leaderElection == null) {
                logger.warn("Handle append entries request of topic {} partition group {} election is null",
                        request.getTopic(), request.getPartitionGroup());
                return new Command(new JoyQueueHeader(Direction.RESPONSE, CommandType.RAFT_APPEND_ENTRIES_RESPONSE),
                        new AppendEntriesResponse.Build().success(false).nextPosition(-1L).build());
            }

            Command response =  leaderElection.handleAppendEntriesRequest(request);
            if (null == response) {
                response = new Command(new JoyQueueHeader(Direction.RESPONSE, CommandType.RAFT_APPEND_ENTRIES_RESPONSE),
                        new AppendEntriesResponse.Build().success(false).nextPosition(-1L).build());
            }
            return response;
        } catch (Exception e) {
            logger.warn("Handle append entries request of topic {} partition group {} fail",
                    request.getTopic(), request.getPartitionGroup(), e);
            return new Command(new JoyQueueHeader(Direction.RESPONSE, CommandType.RAFT_APPEND_ENTRIES_RESPONSE),
                               new AppendEntriesResponse.Build().success(false).nextPosition(-1L).build());
        }

    }

    @Override
    public int type() {
        return CommandType.RAFT_APPEND_ENTRIES_REQUEST;
    }
}
