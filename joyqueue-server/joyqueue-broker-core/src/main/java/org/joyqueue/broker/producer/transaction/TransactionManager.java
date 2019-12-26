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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.broker.buffer.Serializer;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.monitor.BrokerMonitor;
import org.joyqueue.broker.producer.ProduceConfig;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.QosLevel;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.message.BrokerCommit;
import org.joyqueue.message.BrokerPrepare;
import org.joyqueue.message.BrokerRollback;
import org.joyqueue.network.session.Producer;
import org.joyqueue.network.session.TransactionId;
import org.joyqueue.store.PartitionGroupStore;
import org.joyqueue.store.StoreService;
import org.joyqueue.store.WriteRequest;
import org.joyqueue.store.WriteResult;
import org.joyqueue.store.message.MessageParser;
import org.joyqueue.store.transaction.TransactionStore;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
    private ClusterManager clusterManager;
    private BrokerMonitor brokerMonitor;

    private UnCompletedTransactionManager unCompletedTransactionManager;
    private TransactionRecover transactionRecover;
    private TransactionCleaner transactionCleaner;

    public TransactionManager(ProduceConfig config, StoreService store, ClusterManager clusterManager, BrokerMonitor brokerMonitor) {
        this.config = config;
        this.store = store;
        this.clusterManager = clusterManager;
        this.brokerMonitor = brokerMonitor;
    }

    public TransactionId prepare(Producer producer, BrokerPrepare prepare) throws JoyQueueException {
        if (unCompletedTransactionManager.getTransactionCount(producer.getTopic(), producer.getApp()) > config.getTransactionMaxUncomplete()) {
            logger.warn("too many transactions, topic: {}, app: {}. txId: {}", prepare.getTopic(), prepare.getApp(), prepare.getTxId());
            throw new JoyQueueException(JoyQueueCode.FW_TRANSACTION_LIMIT);
        }

        TransactionStore transactionStore = store.getTransactionStore(producer.getTopic());
        if (transactionStore == null) {
            logger.error("transaction store not exist, topic: {}", producer.getTopic());
            throw new JoyQueueException(JoyQueueCode.CN_TRANSACTION_NOT_EXISTS);
        }

        int storeId = transactionStore.next();
        TransactionId transactionId = generateTransactionId(prepare, storeId);

        try {
            unCompletedTransactionManager.putTransaction(transactionId);

            ByteBuffer buffer = ByteBuffer.allocate(Serializer.sizeOfBrokerPrepare(prepare));
            Serializer.writeBrokerPrepare(prepare, buffer);
            buffer.flip();

            Future<WriteResult> prepareFuture = transactionStore.asyncWrite(storeId, buffer.slice());
            waitFuture(producer, prepareFuture);
        } catch (Exception e) {
            try {
                transactionStore.remove(storeId);
                unCompletedTransactionManager.removeTransaction(transactionId);
            } catch (Exception ex) {
                logger.error("clear prepare exception, topic: {}, app: {}. txId: {}", prepare.getTopic(), prepare.getApp(), prepare.getTxId(), ex);
            }

            logger.error("write prepare exception, topic: {}, app: {}. txId: {}", prepare.getTopic(), prepare.getApp(), prepare.getTxId(), e);
            throw new JoyQueueException(JoyQueueCode.CN_TRANSACTION_PREPARE_ERROR);
        }

        return transactionId;
    }

    protected TransactionId generateTransactionId(BrokerPrepare prepare, int storeId) {
        long now = SystemClock.now();
        String txId = prepare.getTxId();
        if (StringUtils.isBlank(txId)) {
            txId = String.format("transactionId_%s_%s_%s_%s", prepare.getTopic(), prepare.getApp(), sequence.getAndIncrement(), now);
        }
        return new TransactionId(prepare.getTopic(), prepare.getApp(), txId, prepare.getQueryId(), storeId, prepare.getSource(), prepare.getTimeout(), now);
    }

    public TransactionId commit(final Producer producer, final BrokerCommit commit) throws JoyQueueException {
        TransactionStore transactionStore = store.getTransactionStore(producer.getTopic());
        if (transactionStore == null) {
            logger.error("transaction store not exist, topic: {}", producer.getTopic());
            throw new JoyQueueException(JoyQueueCode.CN_TRANSACTION_NOT_EXISTS);
        }

        TransactionId transactionId = unCompletedTransactionManager.getTransaction(commit.getTopic(), commit.getApp(), commit.getTxId());
        if (transactionId == null) {
            logger.debug("The current tx is not in txManager, topic: {}, app: {}, txId: {}", commit.getTxId(), commit.getApp(), commit.getTxId());
            throw new JoyQueueException(JoyQueueCode.CN_TRANSACTION_NOT_EXISTS);
        }

        BrokerPrepare brokerPrepare = null;
        Map<Integer, List<WriteRequest>> writeRequestMap = Maps.newHashMap();
        int messageSize = 0;

        try {
            int index = 0;
            Iterator<ByteBuffer> readIterator = transactionStore.readIterator(transactionId.getStoreId());
            while (readIterator.hasNext()) {
                ByteBuffer byteBuffer = readIterator.next();
                if (index == 0) {
                    brokerPrepare = Serializer.readBrokerPrepare(byteBuffer);
                } else {
                    short currentPartition = dispatchPartition(byteBuffer, (short) 0);
                    PartitionGroup partitionGroup = clusterManager.getPartitionGroup(TopicName.parse(producer.getTopic()), currentPartition);
                    if (partitionGroup == null) {
                        throw new JoyQueueException(JoyQueueCode.SE_WRITE_FAILED);
                    }
                    List<WriteRequest> writeRequests = writeRequestMap.get(partitionGroup.getGroup());
                    if (writeRequests == null) {
                        writeRequests = Lists.newLinkedList();
                        writeRequestMap.put(partitionGroup.getGroup(), writeRequests);
                    }
                    writeRequests.add(new WriteRequest(currentPartition, byteBuffer, 1));
                    messageSize += byteBuffer.limit();
                }
                index++;
            }

            try {
                for (Map.Entry<Integer, List<WriteRequest>> entry : writeRequestMap.entrySet()) {
                    PartitionGroupStore partitionStoreService = store.getStore(commit.getTopic(), entry.getKey(), QosLevel.REPLICATION);
                    long startTime = SystemClock.now();
                    Future<WriteResult> future = partitionStoreService.asyncWrite(entry.getValue().toArray(new WriteRequest[0]));
                    waitFuture(producer, future);
                    long endTime = SystemClock.now();

                    for (WriteRequest writeRequest : entry.getValue()) {
                        brokerMonitor.onPutMessage(producer.getTopic(), producer.getApp(), entry.getKey(), writeRequest.getPartition(), 1, messageSize, endTime - startTime);
                    }
                }
            } catch (Exception e) {
                logger.warn("write transaction message exception, topic: {}, app: {}, txId: {}", brokerPrepare.getTopic(), brokerPrepare.getApp(), brokerPrepare.getTxId(), e);
                throw new JoyQueueException(JoyQueueCode.SE_IO_ERROR);
            }

            unCompletedTransactionManager.removeTransaction(transactionId);
        } catch (Exception e) {
            logger.error("write transaction message exception, topic: {}, app: {}, txId: {}", commit.getTopic(), commit.getApp(), commit.getTxId(), e);
            if (e instanceof JoyQueueException) {
                throw (JoyQueueException) e;
            } else {
                throw new JoyQueueException(JoyQueueCode.SE_IO_ERROR);
            }
        }
        return transactionId;
    }

    protected short dispatchPartition(ByteBuffer msg, short defaultPartition) {
        short partition = MessageParser.getShort(msg, MessageParser.PARTITION);
        return partition < 0 ? defaultPartition : partition;
    }

    protected void waitFuture(Producer producer, Future<WriteResult> future) throws JoyQueueException {
        try {
            org.joyqueue.domain.Producer.ProducerPolicy producerPolicy = clusterManager.tryGetProducerPolicy(TopicName.parse(producer.getTopic()), producer.getApp());
            int configTimeOut = (producerPolicy == null ? 0 : producerPolicy.getTimeOut());
            if (configTimeOut == 0) {
                future.get();
            } else {
                future.get(configTimeOut, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            throw new JoyQueueException(JoyQueueCode.SE_DISK_FLUSH_SLOW);
        } catch (ExecutionException | TimeoutException e) {
            throw new JoyQueueException(JoyQueueCode.SE_WRITE_TIMEOUT);
        } catch (Exception e) {
            throw new JoyQueueException(JoyQueueCode.CN_CONNECTION_TIMEOUT);
        }
    }

    public TransactionId rollback(final Producer producer, final BrokerRollback rollback) throws JoyQueueException {
        TransactionStore transactionStore = store.getTransactionStore(producer.getTopic());
        if (transactionStore == null) {
            logger.error("transaction store not exist, topic: {}", producer.getTopic());
            throw new JoyQueueException(JoyQueueCode.CN_TRANSACTION_NOT_EXISTS);
        }

        TransactionId transactionId = unCompletedTransactionManager.getTransaction(rollback.getTopic(), rollback.getApp(), rollback.getTxId());
        if (transactionId == null) {
            logger.debug("transaction not exist, topic: {}, id: {}", producer.getTopic(), rollback.getTxId());
            throw new JoyQueueException(JoyQueueCode.CN_TRANSACTION_NOT_EXISTS);
        }

        transactionStore.remove(transactionId.getStoreId());
        unCompletedTransactionManager.removeTransaction(transactionId);
        return transactionId;
    }

    public Future<WriteResult> putMessage(Producer producer, String txId, ByteBuffer... byteBuffers) throws JoyQueueException {
        TransactionStore transactionStore = store.getTransactionStore(producer.getTopic());
        if (Strings.isNullOrEmpty(txId)) {
            logger.error("The current message is not a tx message!");
            throw new JoyQueueException(JoyQueueCode.CN_UNKNOWN_ERROR);
        }

        TransactionId transactionId = unCompletedTransactionManager.getTransaction(producer.getTopic(), producer.getApp(), txId);
        if (transactionId == null) {
            logger.debug("The current tx is not in txManager! txId:{}...", txId);
            throw new JoyQueueException(JoyQueueCode.CN_TRANSACTION_NOT_EXISTS);
        }

        return transactionStore.asyncWrite(transactionId.getStoreId(), byteBuffers);
    }

    public TransactionId getTransaction(String topic, String app, String txId) {
        return unCompletedTransactionManager.getTransaction(topic, app, txId);
    }

    public List<TransactionId> getFeedback(Producer producer, int count) {
        return unCompletedTransactionManager.getFeedback(producer, count);
    }

    @Override
    protected void validate() throws Exception {
        unCompletedTransactionManager = new UnCompletedTransactionManager(config);
        transactionRecover = new TransactionRecover(config, unCompletedTransactionManager, store);
        transactionCleaner = new TransactionCleaner(config, unCompletedTransactionManager, store);
    }

    @Override
    protected void doStart() throws Exception {
        transactionRecover.recover();
        transactionCleaner.start();
    }

    @Override
    protected void doStop() {
        if (transactionCleaner != null) {
            transactionCleaner.stop();
        }
    }
}
