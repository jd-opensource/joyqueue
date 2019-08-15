/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.broker.election;

import io.chubao.joyqueue.broker.config.Configuration;
import io.chubao.joyqueue.broker.election.handler.AppendEntriesRequestHandler;
import io.chubao.joyqueue.broker.election.handler.ReplicateConsumePosRequestHandler;
import io.chubao.joyqueue.broker.election.handler.TimeoutNowRequestHandler;
import io.chubao.joyqueue.broker.election.handler.VoteRequestHandler;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import io.chubao.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;

/**
 * Created by zhuduohui on 2018/10/8.
 */
public class ElectionCommandHandlerRegistrarStub {
    public static CommandHandlerFactory register(ElectionManager electionManager, DefaultCommandHandlerFactory commandHandlerFactory) {
        commandHandlerFactory.register(CommandType.RAFT_VOTE_REQUEST, new VoteRequestHandler(electionManager));
        commandHandlerFactory.register(CommandType.RAFT_APPEND_ENTRIES_REQUEST, new AppendEntriesRequestHandler(electionManager));
        commandHandlerFactory.register(CommandType.RAFT_TIMEOUT_NOW_REQUEST, new TimeoutNowRequestHandler(electionManager));
        commandHandlerFactory.register(CommandType.REPLICATE_CONSUME_POS_REQUEST, new ReplicateConsumePosRequestHandler(
                new ElectionConfig(new Configuration()), new ConsumeStub()));
        return commandHandlerFactory;
    }
}
