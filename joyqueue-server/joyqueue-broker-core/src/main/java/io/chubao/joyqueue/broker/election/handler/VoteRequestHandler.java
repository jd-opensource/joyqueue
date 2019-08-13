/**
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
package io.chubao.joyqueue.broker.election.handler;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.election.ElectionManager;
import io.chubao.joyqueue.broker.election.ElectionService;
import io.chubao.joyqueue.broker.election.RaftLeaderElection;
import io.chubao.joyqueue.broker.election.command.VoteRequest;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandler;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.exception.TransportException;

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
