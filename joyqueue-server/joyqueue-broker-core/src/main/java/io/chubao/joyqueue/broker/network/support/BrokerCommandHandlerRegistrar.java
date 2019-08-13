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
package io.chubao.joyqueue.broker.network.support;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.election.handler.AppendEntriesRequestHandler;
import io.chubao.joyqueue.broker.election.handler.ReplicateConsumePosRequestHandler;
import io.chubao.joyqueue.broker.election.handler.TimeoutNowRequestHandler;
import io.chubao.joyqueue.broker.election.handler.VoteRequestHandler;
import io.chubao.joyqueue.broker.handler.CreatePartitionGroupHandler;
import io.chubao.joyqueue.broker.handler.PartitionGroupLeaderChangeHandler;
import io.chubao.joyqueue.broker.handler.RemovePartitionGroupHandler;
import io.chubao.joyqueue.broker.handler.UpdatePartitionGroupHandler;
import io.chubao.joyqueue.broker.index.handler.ConsumeIndexQueryHandler;
import io.chubao.joyqueue.broker.index.handler.ConsumeIndexStoreHandler;
import io.chubao.joyqueue.broker.producer.transaction.handler.TransactionCommitRequestHandler;
import io.chubao.joyqueue.broker.producer.transaction.handler.TransactionRollbackRequestHandler;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.chubao.joyqueue.server.retry.remote.handler.RemoteRetryMessageHandler;

/**
 * BrokerCommandHandlerRegistrar
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/17
 */
// 用BrokerCommandHandler作为处理接口，通过spi方式加载
@Deprecated
public class BrokerCommandHandlerRegistrar {

    public static void register(BrokerContext brokerContext, DefaultCommandHandlerFactory commandHandlerFactory) {
        //  retry
        RemoteRetryMessageHandler remoteRetryMessageHandler = new RemoteRetryMessageHandler(brokerContext.getRetryManager());
        commandHandlerFactory.register(CommandType.PUT_RETRY, remoteRetryMessageHandler);
        commandHandlerFactory.register(CommandType.GET_RETRY, remoteRetryMessageHandler);
        commandHandlerFactory.register(CommandType.UPDATE_RETRY, remoteRetryMessageHandler);
        commandHandlerFactory.register(CommandType.GET_RETRY_COUNT, remoteRetryMessageHandler);

        // raft related command
        commandHandlerFactory.register(CommandType.RAFT_VOTE_REQUEST, new VoteRequestHandler(brokerContext));
        commandHandlerFactory.register(CommandType.RAFT_APPEND_ENTRIES_REQUEST, new AppendEntriesRequestHandler(brokerContext));
        commandHandlerFactory.register(CommandType.RAFT_TIMEOUT_NOW_REQUEST, new TimeoutNowRequestHandler(brokerContext));
        commandHandlerFactory.register(CommandType.REPLICATE_CONSUME_POS_REQUEST, new ReplicateConsumePosRequestHandler(brokerContext));

        // consume position related command
        commandHandlerFactory.register(CommandType.CONSUME_INDEX_QUERY_REQUEST, new ConsumeIndexQueryHandler(brokerContext));
        commandHandlerFactory.register(CommandType.CONSUME_INDEX_STORE_REQUEST, new ConsumeIndexStoreHandler(brokerContext));

        //nameserver
        commandHandlerFactory.register(NsrCommandType.NSR_CREATE_PARTITIONGROUP, new CreatePartitionGroupHandler(brokerContext));
        commandHandlerFactory.register(NsrCommandType.NSR_UPDATE_PARTITIONGROUP, new UpdatePartitionGroupHandler(brokerContext));
        commandHandlerFactory.register(NsrCommandType.NSR_REMOVE_PARTITIONGROUP, new RemovePartitionGroupHandler(brokerContext));
        commandHandlerFactory.register(NsrCommandType.NSR_LEADERCHANAGE_PARTITIONGROUP, new PartitionGroupLeaderChangeHandler(brokerContext));

        // transaction
        commandHandlerFactory.register(CommandType.TRANSACTION_COMMIT_REQUEST, new TransactionCommitRequestHandler(brokerContext));
        commandHandlerFactory.register(CommandType.TRANSACTION_ROLLBACK_REQUEST, new TransactionRollbackRequestHandler(brokerContext));
    }
}
