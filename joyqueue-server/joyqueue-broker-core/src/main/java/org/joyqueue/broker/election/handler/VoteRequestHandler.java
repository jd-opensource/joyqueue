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
import org.joyqueue.broker.election.RaftLeaderElection;
import org.joyqueue.broker.election.command.VoteRequest;
import org.joyqueue.broker.election.command.VoteResponse;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.network.transport.command.handler.CommandHandler;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.exception.TransportException;

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
        if (!(brokerContext.getElectionService() instanceof ElectionManager)) {
            throw new IllegalArgumentException();
        }
        this.electionManager = (ElectionManager)brokerContext.getElectionService();
    }

    public VoteRequestHandler(ElectionService electionService) {
        if (!(electionService instanceof ElectionManager)) {
            throw new IllegalArgumentException();
        }
        this.electionManager = (ElectionManager)electionService;
    }

    @Override
    public int type() {
        return CommandType.RAFT_VOTE_REQUEST;
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException{
        if (command == null) {
            logger.error("Receive vote request command is null");
            return null;
        }

        if (!(command.getPayload() instanceof VoteRequest)) {
            throw new IllegalArgumentException();
        }
        VoteRequest voteRequest = (VoteRequest) command.getPayload();
        Command response = null;
        try {
            RaftLeaderElection election = (RaftLeaderElection) electionManager.getLeaderElection(
                    voteRequest.getTopic(), voteRequest.getPartitionGroup());
            if (election == null) {
                logger.warn("Receive vote request of {}, election is null", voteRequest.getTopicPartitionGroup());
            } else  if (voteRequest.isPreVote()) {
                response = election.handlePreVoteRequest(voteRequest);
            } else {
                response =  election.handleVoteRequest(voteRequest);
            }
            if(null == response) {
                response = new Command(new JoyQueueHeader(Direction.RESPONSE, CommandType.RAFT_VOTE_RESPONSE),
                        new VoteResponse(voteRequest.getTerm(), voteRequest.getCandidateId(), election == null ? -1 : election.getLocalNodeId(), false));
            }
            return response;
        } catch (Throwable t) {
            logger.warn("Handle vote request exception, request: {}, exception: ", voteRequest, t);
            return  new Command(new JoyQueueHeader(Direction.RESPONSE, CommandType.RAFT_VOTE_RESPONSE),
                    new VoteResponse(voteRequest.getTerm(), voteRequest.getCandidateId(), -1, false));
        }
    }

}
