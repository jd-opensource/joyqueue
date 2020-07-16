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
package org.joyqueue.broker.kafka.coordinator.transaction.synchronizer;

import com.google.common.collect.Lists;
import org.joyqueue.broker.kafka.config.KafkaConfig;
import org.joyqueue.broker.kafka.coordinator.transaction.TransactionIdManager;
import org.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import org.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import org.joyqueue.broker.kafka.coordinator.transaction.helper.TransactionHelper;
import org.joyqueue.broker.producer.transaction.command.TransactionRollbackRequest;
import org.joyqueue.domain.Broker;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.CommandCallback;
import org.joyqueue.network.transport.command.JoyQueueCommand;
import org.joyqueue.network.transport.session.session.TransportSession;
import org.joyqueue.network.transport.session.session.TransportSessionManager;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * TransactionAbortSynchronizer
 *
 * author: gaohaoxiang
 * date: 2019/4/18
 */
public class TransactionAbortSynchronizer extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionAbortSynchronizer.class);

    private KafkaConfig config;
    private TransportSessionManager sessionManager;
    private TransactionIdManager transactionIdManager;

    public TransactionAbortSynchronizer(KafkaConfig config, TransportSessionManager sessionManager, TransactionIdManager transactionIdManager) {
        this.config = config;
        this.sessionManager = sessionManager;
        this.transactionIdManager = transactionIdManager;
    }

    public boolean abort(TransactionMetadata transactionMetadata, Set<TransactionPrepare> prepareList) throws Exception {
        Map<Broker, List<TransactionPrepare>> brokerPrepareMap = TransactionHelper.splitPrepareByBroker(prepareList);
        CountDownLatch latch = new CountDownLatch(brokerPrepareMap.size());
        boolean[] result = {true};

        for (Map.Entry<Broker, List<TransactionPrepare>> entry : brokerPrepareMap.entrySet()) {
            Broker broker = entry.getKey();
            List<TransactionPrepare> brokerPrepareList = entry.getValue();
            TransactionPrepare brokerPrepare = brokerPrepareList.get(0);
            List<String> txIds = Lists.newLinkedList();

            for (TransactionPrepare prepare : brokerPrepareList) {
                String txId = transactionIdManager.generateId(prepare.getTopic(), prepare.getPartition(), prepare.getApp(),
                        prepare.getTransactionId(), prepare.getProducerId(), prepare.getProducerEpoch());
                txIds.add(txId);
            }

            TransportSession session = sessionManager.getOrCreateSession(broker);
            TransactionRollbackRequest transactionRollbackRequest = new TransactionRollbackRequest(brokerPrepare.getTopic(), brokerPrepare.getApp(), txIds);
            session.async(new JoyQueueCommand(transactionRollbackRequest), config.getTransactionSyncTimeout(), new CommandCallback() {
                @Override
                public void onSuccess(Command request, Command response) {
                    if (response.getHeader().getStatus() != JoyQueueCode.SUCCESS.getCode() &&
                            response.getHeader().getStatus() != JoyQueueCode.CN_TRANSACTION_NOT_EXISTS.getCode()) {
                        logger.error("abort transaction error, broker: {}, request: {}", broker, transactionRollbackRequest);
                        result[0] = false;
                    }

                    latch.countDown();
                }

                @Override
                public void onException(Command request, Throwable cause) {
                    logger.error("abort transaction error, broker: {}, request: {}", broker, transactionRollbackRequest, cause);
                    result[0] = false;
                    latch.countDown();
                }
            });
        }

        if (!latch.await(config.getTransactionSyncTimeout(), TimeUnit.MILLISECONDS)) {
            logger.error("abort transaction timeout, metadata: {}", prepareList);
            return false;
        }

        return result[0];
    }
}