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
package org.joyqueue.broker.producer.transaction;

import org.joyqueue.broker.producer.ProduceConfig;
import org.joyqueue.network.session.Producer;
import org.joyqueue.store.StoreService;
import org.joyqueue.store.transaction.TransactionStore;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * UnCompletedTransactionManager
 *
 * author: gaohaoxiang
 * date: 2019/1/2
 */
public class UnCompletedTransactionManager {
    private static final Logger logger = LoggerFactory.getLogger(UnCompletedTransactionManager.class);
    private ProduceConfig config;

    // 这里使用一个Map来保存正在执行反查的事务ID，可以确保正常情况下一个事务不会被超过多个Producer同时拉去反查。
    private final Map<TransactionId , Long /* Timestamp of the next feedback */> feedbackTransactions = new ConcurrentHashMap<>();


    private final StoreService storeService;


    public UnCompletedTransactionManager(ProduceConfig config, StoreService storeService) {
        this.config = config;
        this.storeService = storeService;
    }

    void removeTransaction(TransactionId transactionId) {
        feedbackTransactions.remove(transactionId);
    }

    public TransactionId getTransaction(String topic, String app, String txId) {
        return openingTransactionsStream(storeService.getTransactionStores(topic).stream())
                .filter(transactionId -> txId.equals(transactionId.getTxId())).findAny().orElse(null);
    }

    private Stream<TransactionId> openingTransactionsStream(Stream<TransactionStore> storeStream) {
        return storeStream
                .flatMap(transactionStore -> {
                    try {
                        return transactionStore.getOpeningTransactions().stream();
                    } catch (ExecutionException | InterruptedException e) {
                        logger.warn("Query opening transactions exception! ", e);
                    }
                    return Stream.empty();
                }).map(ctx -> {
                    BrokerPrepareContext brokerPrepareContext = BrokerPrepareContext.fromMap(ctx.context());
                    return new TransactionId(
                            brokerPrepareContext.getTopic(),
                            brokerPrepareContext.getApp(),
                            brokerPrepareContext.getTxId(),
                            brokerPrepareContext.getQueryId(),
                            ctx.transactionId(),
                            brokerPrepareContext.getSource(),
                            brokerPrepareContext.getTimeout(),
                            brokerPrepareContext.getStartTime(),
                            brokerPrepareContext.getPartition()
                    );
                });
    }
    public List<TransactionId> getFeedback(Producer producer, int count) {

        return openingTransactionsStream(storeService.getTransactionStores(producer.getTopic()).stream())
                .filter(transactionId -> {
            // no need feed back.
            if (!transactionId.isFeedback()) {
                return false;
            }
            Long timestamp = feedbackTransactions.get(transactionId);
            // Put the transaction into map again with updated timestamp
            if (timestamp == null) { // Not exists in the map
                // Put it into the map and wait a feedback timeout,
                // in case of multiple produces feedback same transaction concurrently.
                feedbackTransactions.putIfAbsent(transactionId, SystemClock.now() + config.getFeedbackTimeout());
                return false;
            } else return timestamp > SystemClock.now()
                    && feedbackTransactions.remove(transactionId, timestamp) // Exists but expired and remove successfully.
                    && feedbackTransactions.putIfAbsent(transactionId, SystemClock.now() + config.getFeedbackTimeout()) == null;
        }).limit(count).collect(Collectors.toList());
    }

    public Collection<TransactionId> getTransactions() {
        return openingTransactionsStream(storeService.getAllTransactionStores().stream())
                .collect(Collectors.toList());
    }
}