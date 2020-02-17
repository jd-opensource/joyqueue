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
package org.joyqueue.broker.network.support;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.consumer.handler.ReplicateConsumePosRequestHandler;
import org.joyqueue.broker.handler.CreatePartitionGroupHandler;
import org.joyqueue.broker.handler.PartitionGroupLeaderChangeHandler;
import org.joyqueue.broker.handler.RemovePartitionGroupHandler;
import org.joyqueue.broker.handler.UpdatePartitionGroupHandler;
import org.joyqueue.broker.index.handler.ConsumeIndexQueryHandler;
import org.joyqueue.broker.index.handler.ConsumeIndexStoreHandler;
import org.joyqueue.broker.producer.transaction.handler.TransactionCommitRequestHandler;
import org.joyqueue.broker.producer.transaction.handler.TransactionRollbackRequestHandler;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import org.joyqueue.nsr.network.command.NsrCommandType;
import org.joyqueue.server.retry.remote.handler.RemoteRetryMessageHandler;

/**
 * BrokerCommandHandlerRegistrar
 *
 * author: gaohaoxiang
 * date: 2018/9/17
 */
// 用BrokerCommandHandler作为处理接口，通过spi方式加载
@Deprecated
public class BrokerCommandHandlerRegistrar {

    public static void register(BrokerContext brokerContext, DefaultCommandHandlerFactory commandHandlerFactory) {
        //  retry
        RemoteRetryMessageHandler remoteRetryMessageHandler = new RemoteRetryMessageHandler(brokerContext.getRetryManager(), brokerContext.getPropertySupplier());
        commandHandlerFactory.register(CommandType.PUT_RETRY, remoteRetryMessageHandler);
        commandHandlerFactory.register(CommandType.GET_RETRY, remoteRetryMessageHandler);
        commandHandlerFactory.register(CommandType.UPDATE_RETRY, remoteRetryMessageHandler);
        commandHandlerFactory.register(CommandType.GET_RETRY_COUNT, remoteRetryMessageHandler);

        // consume position related command
        commandHandlerFactory.register(CommandType.CONSUME_INDEX_QUERY_REQUEST, new ConsumeIndexQueryHandler(brokerContext));
        commandHandlerFactory.register(CommandType.CONSUME_INDEX_STORE_REQUEST, new ConsumeIndexStoreHandler(brokerContext));
        commandHandlerFactory.register(CommandType.REPLICATE_CONSUME_POS_REQUEST, new ReplicateConsumePosRequestHandler(brokerContext));

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
