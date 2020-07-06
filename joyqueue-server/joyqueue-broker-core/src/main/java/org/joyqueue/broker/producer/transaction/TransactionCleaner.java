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

import com.google.common.collect.Lists;
import org.joyqueue.broker.producer.ProduceConfig;
import org.joyqueue.network.session.TransactionId;
import org.joyqueue.store.StoreService;
import org.joyqueue.store.transaction.TransactionStore;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TransactionCleaner
 *
 * author: gaohaoxiang
 * date: 2019/1/2
 */
public class TransactionCleaner extends Service implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionCleaner.class);

    private ProduceConfig config;
    private UnCompletedTransactionManager unCompletedTransactionManager;
    private StoreService store;

    private ScheduledExecutorService clearThreadPool;

    public TransactionCleaner(ProduceConfig config, UnCompletedTransactionManager unCompletedTransactionManager, StoreService store) {
        this.config = config;
        this.unCompletedTransactionManager = unCompletedTransactionManager;
        this.store = store;
    }

    @Override
    protected void validate() throws Exception {
        clearThreadPool = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("joyqueue-transaction-clear"));
    }

    @Override
    protected void doStart() throws Exception {
        clearThreadPool.scheduleWithFixedDelay(this, config.getTransactionExpireClearInterval(), config.getTransactionExpireClearInterval(), TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doStop() {
        if (clearThreadPool != null) {
            clearThreadPool.shutdown();
        }
    }

    @Override
    public void run() {
        List<TransactionId> expiredTransactions = Lists.newLinkedList();
        ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String, TransactionId>>> appTransactions = unCompletedTransactionManager.getTransactions();
        for (Map.Entry<String, ConcurrentMap<String, ConcurrentMap<String, TransactionId>>> appEntry : appTransactions.entrySet()) {
            for (Map.Entry<String, ConcurrentMap<String, TransactionId>> topicEntry : appEntry.getValue().entrySet()) {
                ConcurrentMap<String, TransactionId> transactions = topicEntry.getValue();
                for (Map.Entry<String, TransactionId> transactionEntry : transactions.entrySet()) {
                    TransactionId transactionId = transactionEntry.getValue();
                    if (isExpired(transactionEntry.getValue())) {
                        expiredTransactions.add(transactionId);
                    }
                }
            }
        }

        for (TransactionId expiredTransaction : expiredTransactions) {
            doClear(expiredTransaction);
        }
    }

    protected boolean isExpired(TransactionId transactionId) {
        // 需要补偿的需等到过期才清理，不需要补偿的超时就可以清理
        if (transactionId.isFeedback()) {
            return transactionId.isExpired(config.getTransactionExpireTime());
        } else {
            return transactionId.isTimeout();
        }
    }

    protected void doClear(TransactionId transactionId) {
        try {
            TransactionStore transactionStore = store.getTransactionStore(transactionId.getTopic());
            if (transactionStore == null) {
                logger.error("clear expired transaction error, store not exist, topic: {}, app : {}, txId: {}, storeId: {}",
                        transactionId.getTopic(), transactionId.getApp(), transactionId.getTxId(), transactionId.getStoreId());
                return;
            }
            Iterator<ByteBuffer> rByteBufferIterator = transactionStore.readIterator(transactionId.getStoreId());
            if (rByteBufferIterator == null) {
                logger.error("clear expired transaction error, store iterator not exist, topic: {}, app : {}, txId: {}, storeId: {}",
                        transactionId.getTopic(), transactionId.getApp(), transactionId.getTxId(), transactionId.getStoreId());
                return;
            }
            transactionStore.remove(transactionId.getStoreId());
            unCompletedTransactionManager.removeTransaction(transactionId.getTopic(), transactionId.getApp(), transactionId.getTxId());
            logger.info("clear expired transaction, topic: {}, app : {}, txId: {}, storeId: {}", transactionId.getTopic(), transactionId.getApp(), transactionId.getTxId(), transactionId.getStoreId());
        } catch (Exception e) {
            logger.error("clear expired transaction exception, topic: {}, app : {}, txId: {}, storeId: {}",
                    transactionId.getTopic(), transactionId.getApp(), transactionId.getTxId(), transactionId.getStoreId(), e);
        }
    }
}