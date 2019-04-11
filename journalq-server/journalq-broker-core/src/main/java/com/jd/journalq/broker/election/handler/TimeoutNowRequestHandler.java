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
package com.jd.journalq.broker.election.handler;

import com.jd.journalq.broker.election.command.TimeoutNowRequest;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.election.ElectionManager;
import com.jd.journalq.broker.election.ElectionService;
import com.jd.journalq.broker.election.RaftLeaderElection;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.network.transport.command.handler.CommandHandler;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.exception.TransportException;
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
