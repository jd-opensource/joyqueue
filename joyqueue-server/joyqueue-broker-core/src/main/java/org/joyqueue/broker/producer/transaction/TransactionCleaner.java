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
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final UnCompletedTransactionManager unCompletedTransactionManager;
    private final TransactionManager transactionManager;

    private ScheduledExecutorService clearThreadPool;

    public TransactionCleaner(ProduceConfig config, UnCompletedTransactionManager unCompletedTransactionManager, TransactionManager transactionManager) {
        this.config = config;
        this.unCompletedTransactionManager = unCompletedTransactionManager;
        this.transactionManager = transactionManager;
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
        unCompletedTransactionManager.getTransactions().stream()
                .filter(this::isExpired)
                .forEach(this::doClear);
    }

    protected boolean isExpired(TransactionId transactionId) {
        // 需要补偿的需等到过期才清理，不需要补偿的超时就可以清理
        if (transactionId.isFeedback()) {
            return transactionId.isExpired(config.getTransactionExpireTime());
        } else {
            return transactionId.isTimeout();
        }
    }

    private void doClear(TransactionId transactionId) {

        try {
            transactionManager.completeTransaction(false, transactionId);
            logger.info("clear expired transaction, topic: {}, app : {}, txId: {}, storeId: {}", transactionId.getTopic(), transactionId.getApp(), transactionId.getTxId(), transactionId.getStoreId());
        } catch (Exception e) {
            logger.error("clear expired transaction exception, topic: {}, app : {}, txId: {}, storeId: {}",
                    transactionId.getTopic(), transactionId.getApp(), transactionId.getTxId(), transactionId.getStoreId(), e);
        }
    }
}