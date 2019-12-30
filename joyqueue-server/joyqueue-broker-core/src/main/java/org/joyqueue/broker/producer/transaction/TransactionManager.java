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

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.broker.producer.ProduceConfig;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.message.BrokerCommit;
import org.joyqueue.message.BrokerPrepare;
import org.joyqueue.message.BrokerRollback;
import org.joyqueue.network.session.Producer;
import org.joyqueue.store.StoreService;
import org.joyqueue.store.WriteRequest;
import org.joyqueue.store.WriteResult;
import org.joyqueue.store.transaction.StoreTransactionContext;
import org.joyqueue.store.transaction.TransactionStore;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 事务管理器
 *
 * @author lining11
 * Date: 2018/8/17
 */
public class TransactionManager extends Service {

    private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);

    private final AtomicLong sequence = new AtomicLong();

    private ProduceConfig config;
    private StoreService store;

    private UnCompletedTransactionManager unCompletedTransactionManager;
    private TransactionCleaner transactionCleaner;

    public TransactionManager(ProduceConfig config, StoreService store) {
        this.config = config;
        this.store = store;
    }
    // TODO 处理too many transactions 异常
    public TransactionId prepare(Producer producer, BrokerPrepare prepare) throws JoyQueueException {
        try {
            TransactionStore transactionStore = store.getTransactionStore(producer.getTopic(), prepare.getPartition());
            if (transactionStore == null) {
                logger.error("transaction store not exist, topic: {}", producer.getTopic());
                throw new JoyQueueException(JoyQueueCode.CN_TRANSACTION_NOT_EXISTS);
            }
            BrokerPrepareContext brokerPrepareContext = BrokerPrepareContext.fromBrokerPrepare(prepare);
            brokerPrepareContext.setTxId(getOrGenerateTransactionId(prepare));
            StoreTransactionContext context = transactionStore.createTransaction(brokerPrepareContext.toMap());
            return new TransactionId(
                    brokerPrepareContext.getTopic(),
                    brokerPrepareContext.getApp(),
                    brokerPrepareContext.getTxId(),
                    brokerPrepareContext.getQueryId(),
                    context.transactionId(),
                    brokerPrepareContext.getSource(),
                    brokerPrepareContext.getTimeout(),
                    brokerPrepareContext.getStartTime(),
                    brokerPrepareContext.getPartition()
            );

        } catch (Exception e) {
            logger.error("write prepare exception, topic: {}, app: {}. txId: {}", prepare.getTopic(), prepare.getApp(), prepare.getTxId(), e);
            throw new JoyQueueException(JoyQueueCode.CN_TRANSACTION_PREPARE_ERROR);
        }

    }

    private String getOrGenerateTransactionId(BrokerPrepare prepare) {
        long now = SystemClock.now();
        String txId = prepare.getTxId();
        if (StringUtils.isBlank(txId)) {
            txId = String.format("transactionId_%s_%s_%s_%s", prepare.getTopic(), prepare.getApp(), sequence.getAndIncrement(), now);
        }

        return txId;
    }

    public TransactionId commit(final BrokerCommit commit) throws JoyQueueException {
        return completeTransaction( commit.getTopic(), commit.getApp(), commit.getTxId(), true);
    }

    private TransactionId completeTransaction(String topic, String app, String txId, final boolean commitOrRollback) throws JoyQueueException {

        TransactionId transactionId = unCompletedTransactionManager.getTransaction(topic, app, txId);
        if (transactionId == null) {
            logger.debug("The current tx is not in txManager, topic: {}, app: {}, txId: {}", topic, app, txId);
            throw new JoyQueueException(JoyQueueCode.CN_TRANSACTION_NOT_EXISTS);
        }
        return completeTransaction(commitOrRollback, transactionId);
    }

    TransactionId completeTransaction(boolean commitOrRollback, TransactionId transactionId) throws JoyQueueException {
        TransactionStore transactionStore = store.getTransactionStore(transactionId.getTopic(), transactionId.getPartition());
        if (transactionStore == null) {
            logger.error("transaction store not exist, topic: {}", transactionId.getTopic());
            throw new JoyQueueException(JoyQueueCode.CN_TRANSACTION_NOT_EXISTS);
        }
        try {
            transactionStore.completeTransaction(transactionId.getStoreId(), true);
            unCompletedTransactionManager.removeTransaction(transactionId);
        } catch (Exception e) {
            logger.error("Commit exception, topic: {}, app: {}, txId: {}", transactionId.getTopic(), transactionId.getApp(), transactionId.getTxId(), e);
            throw new JoyQueueException(commitOrRollback ? JoyQueueCode.CN_TRANSACTION_COMMIT_ERROR : JoyQueueCode.CN_TRANSACTION_ROLLBACK_ERROR);
        }
        return transactionId;
    }

    public TransactionId rollback(final BrokerRollback rollback) throws JoyQueueException {
        return completeTransaction(rollback.getTopic(), rollback.getApp(), rollback.getTxId(), false);
    }

    public Future<WriteResult> putMessage(Producer producer, String txId, ByteBuffer... byteBuffers) throws JoyQueueException {

        TransactionId transactionId = unCompletedTransactionManager.getTransaction(producer.getTopic(), producer.getApp(), txId);
        if (transactionId == null) {
            logger.debug("The current tx is not in txManager! txId:{}...", txId);
            throw new JoyQueueException(JoyQueueCode.CN_TRANSACTION_NOT_EXISTS);
        }


        TransactionStore transactionStore = store.getTransactionStore(producer.getTopic(), transactionId.getPartition());
        if (Strings.isNullOrEmpty(txId)) {
            logger.error("The current message is not a tx message!");
            throw new JoyQueueException(JoyQueueCode.CN_UNKNOWN_ERROR);
        }

        return transactionStore.asyncWrite(transactionId.getStoreId(),
                Arrays.stream(byteBuffers).map(buffer -> new WriteRequest(transactionId.getPartition(), buffer)).toArray(WriteRequest[]::new));
    }

    public TransactionId getTransaction(String topic, String app, String txId) {
        return unCompletedTransactionManager.getTransaction(topic, app, txId);
    }

    public List<TransactionId> getFeedback(Producer producer, int count) {
        return unCompletedTransactionManager.getFeedback(producer, count);
    }

    @Override
    protected void validate() throws Exception {
        unCompletedTransactionManager = new UnCompletedTransactionManager(config, store);
        transactionCleaner = new TransactionCleaner(config, unCompletedTransactionManager, this);
    }

    @Override
    protected void doStart() throws Exception {
        transactionCleaner.start();
    }

    @Override
    protected void doStop() {
        if (transactionCleaner != null) {
            transactionCleaner.stop();
        }
    }
}
