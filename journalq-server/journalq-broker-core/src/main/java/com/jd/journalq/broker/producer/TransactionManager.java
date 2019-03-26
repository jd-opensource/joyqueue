package com.jd.journalq.broker.producer;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.buffer.Serializer;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.monitor.BrokerMonitor;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.message.BrokerCommit;
import com.jd.journalq.message.BrokerPrepare;
import com.jd.journalq.message.BrokerRollback;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.session.TransactionId;
import com.jd.journalq.store.PartitionGroupStore;
import com.jd.journalq.store.StoreService;
import com.jd.journalq.store.WriteRequest;
import com.jd.journalq.store.WriteResult;
import com.jd.journalq.store.message.MessageParser;
import com.jd.journalq.store.transaction.TransactionStore;
import com.jd.journalq.toolkit.lang.Strings;
import com.jd.journalq.toolkit.service.Service;
import com.jd.journalq.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 事务管理器
 *
 * @author lining11
 * Date: 2018/8/17
 */
public class TransactionManager extends Service {

    private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);

    private ProduceConfig config;
    private StoreService store;
    private ClusterManager clusterManager;
    private BrokerMonitor brokerMonitor;

    private UnCompleteTransactionManager unCompleteTransactionManager;
    private TransactionRecover transactionRecover;
    private TransactionCleaner transactionCleaner;

    public TransactionManager(ProduceConfig config, StoreService store, ClusterManager clusterManager, BrokerMonitor brokerMonitor) {
        this.config = config;
        this.store = store;
        this.clusterManager = clusterManager;
        this.brokerMonitor = brokerMonitor;
    }

    public void txPrepare(final Producer producer, final BrokerPrepare prepare) throws JMQException {
        TransactionStore transactionStore = store.getTransactionStore(producer.getTopic());
        int storeId = transactionStore.next();
        TransactionId transactionId = new TransactionId(prepare.getTopic(), prepare.getApp(), prepare.getTxId(), prepare.getQueryId(), storeId, prepare.getTimeout(), SystemClock.now());
        Future<WriteResult> prepareFuture = null;

        try {
            ByteBuffer buffer = ByteBuffer.allocate(Serializer.sizeOfBrokerPrepare(prepare));
            Serializer.writeBrokerPrepare(prepare, buffer);
            buffer.flip();
            prepareFuture = transactionStore.asyncWrite(storeId, buffer.slice());
        } catch (Exception e) {
            logger.error("write prepare exception, topic: {}, app: {}. txId: {}", prepare.getTopic(), prepare.getApp(), prepare.getTxId(), e);
            throw new JMQException(JMQCode.CN_TRANSACTION_PREPARE_ERROR);
        }

        try {
            prepareFuture.get(prepare.getTimeout(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("write prepare wait exception, topic: {}, app: {}. txId: {}", prepare.getTopic(), prepare.getApp(), prepare.getTxId(), e);
            throw new JMQException(JMQCode.SE_WRITE_TIMEOUT);
        }
        unCompleteTransactionManager.putTransaction(transactionId);
    }

    public void txCommit(final Producer producer, final BrokerCommit commit) throws JMQException {
        TransactionStore transactionStore = store.getTransactionStore(producer.getTopic());
        TransactionId transactionId = unCompleteTransactionManager.getTransaction(commit.getTopic(), commit.getApp(), commit.getTxId());

        if (transactionId == null) {
            logger.error("The current tx is not in txManager, topic: {}, app: {}, txId: {}", commit.getTxId(), commit.getApp(), commit.getTxId());
            throw new JMQException(JMQCode.CN_TRANSACTION_NOT_EXISTS);
        }

        BrokerPrepare brokerPrepare = null;
        short partition = -1;
        PartitionGroup partitionGroup = null;
        List<ByteBuffer> messageBuffers = Lists.newLinkedList();
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
                    PartitionGroup currentPartitionGroup = clusterManager.getPartitionGroup(TopicName.parse(producer.getTopic()), currentPartition);
                    if (partition == -1) {
                        partition = currentPartition;
                    }
                    if (partitionGroup == null) {
                        partitionGroup = currentPartitionGroup;
                    }
                    if (currentPartitionGroup == null || currentPartitionGroup.getGroup() != partitionGroup.getGroup() || currentPartition != partition) {
                        throw new JMQException(JMQCode.SE_WRITE_FAILED);
                    }
                    messageBuffers.add(byteBuffer);
                    messageSize += byteBuffer.limit();
                }
                index++;
            }

            try {
                PartitionGroupStore partitionStoreService = store.getStore(commit.getTopic(), partitionGroup.getGroup(), QosLevel.REPLICATION);
                long startTime = SystemClock.now();
                WriteRequest[] writeRequests = new WriteRequest[messageBuffers.size()];
                for (int i = 0; i < messageBuffers.size(); i++) {
                    writeRequests[i] = new WriteRequest(partition, messageBuffers.get(i));
                }
                Future<WriteResult> future = partitionStoreService.asyncWrite(writeRequests);
                waitFuture(producer, future, commit.getStartTime());
                brokerMonitor.onPutMessage(producer.getTopic(), producer.getApp(), partitionGroup.getGroup(), partition, messageBuffers.size(), messageSize, SystemClock.now() - startTime);
            } catch (Exception e) {
                logger.warn("write transaction message exception, topic: {}, app: {}, txId: {}", brokerPrepare.getTopic(), brokerPrepare.getApp(), brokerPrepare.getTxId(), e);
                throw new JMQException(JMQCode.SE_IO_ERROR);
            }
        } catch (Exception e) {
            logger.error("write transaction message exception, topic: {}, app: {}, txId: {}", commit.getTopic(), commit.getApp(), commit.getTxId(), e);
            if (e instanceof JMQException) {
                throw (JMQException) e;
            } else {
                throw new JMQException(JMQCode.SE_IO_ERROR);
            }
        }
        unCompleteTransactionManager.removeTransaction(commit.getTopic(), commit.getApp(), commit.getTxId());
    }

    /**
     * @param msg              需要dispatch 的消息。
     * @param defaultPartition 当前broker所持有的所有主partition的数组。
     * @return 除指定partition和isOrdered=true,其他情况，返回的结果中，只包含一个结果。
     */
    private short dispatchPartition(ByteBuffer msg, short defaultPartition) {
        short partition = MessageParser.getShort(msg, MessageParser.PARTITION);
        return partition < 0 ? defaultPartition : partition;
    }

    private void waitFuture(Producer producer, Future<WriteResult> future, long receiveTime) throws JMQException {
        try {
            com.jd.journalq.domain.Producer.ProducerPolicy producerPolicy = clusterManager.tryGetProducerPolicy(TopicName.parse(producer.getTopic()), producer.getApp());
            if (producerPolicy == null) {
                throw new JMQException(JMQCode.FW_PRODUCER_NOT_EXISTS);
            }
            int configTimeOut = producerPolicy.getTimeOut();
            if (configTimeOut == 0) {
                future.get();
            } else {
                future.get(configTimeOut, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            throw new JMQException(JMQCode.SE_DISK_FLUSH_SLOW);
        } catch (ExecutionException | TimeoutException e) {
            throw new JMQException(JMQCode.SE_WRITE_TIMEOUT);
        } catch (JMQException e) {
            throw e;
        } catch (Exception e) {
            throw new JMQException(JMQCode.CN_CONNECTION_TIMEOUT);
        }
    }


    public void txRollback(final Producer producer, final BrokerRollback rollback) throws JMQException {
        TransactionStore transactionStore = store.getTransactionStore(producer.getTopic());
        if (transactionStore == null) {
            logger.error("transaction store not exist, topic: {}", producer.getTopic());
            throw new JMQException(JMQCode.CN_TRANSACTION_NOT_EXISTS);
        }

        TransactionId transactionId = unCompleteTransactionManager.getTransaction(rollback.getTopic(), rollback.getApp(), rollback.getTxId());
        if (transactionId == null) {
            logger.warn("transaction not exist, topic: {}, id: {}", producer.getTopic(), rollback.getTxId());
            throw new JMQException(JMQCode.CN_TRANSACTION_NOT_EXISTS);
        }

        transactionStore.remove(transactionId.getStoreId());
        unCompleteTransactionManager.removeTransaction(rollback.getTopic(), rollback.getApp(), rollback.getTxId());
    }

    public Future<WriteResult> txMessage(Producer producer, final String txId, ByteBuffer... byteBuffers) throws JMQException {
        TransactionStore transactionStore = store.getTransactionStore(producer.getTopic());
        if (Strings.isNullOrEmpty(txId)) {
            logger.error("The current message is not a tx message!");
            throw new JMQException(JMQCode.CN_UNKNOWN_ERROR);
        }
        TransactionId transactionId = unCompleteTransactionManager.getTransaction(producer.getTopic(), producer.getApp(), txId);
        if (transactionId == null) {
            logger.error("The current tx is not in txManager! txId:{}...", txId);
            throw new JMQException(JMQCode.CN_TRANSACTION_NOT_EXISTS);
        }
        return transactionStore.asyncWrite(transactionId.getStoreId(), byteBuffers);
    }

    public List<TransactionId> txFeedback(Producer producer, int count) {
        return unCompleteTransactionManager.txFeedback(producer, count);
    }

    @Override
    protected void validate() throws Exception {
        unCompleteTransactionManager = new UnCompleteTransactionManager(config);
        transactionRecover = new TransactionRecover(config, unCompleteTransactionManager, store);
        transactionCleaner = new TransactionCleaner(config, unCompleteTransactionManager, store);
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
