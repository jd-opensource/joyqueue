package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.coordinator.Coordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionState;
import com.jd.journalq.broker.kafka.coordinator.transaction.exception.TransactionException;
import com.jd.journalq.broker.kafka.model.PartitionMetadataAndError;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.toolkit.service.Service;
import com.jd.journalq.toolkit.time.SystemClock;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * TransactionHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/10
 */
// TODO 补充日志
public class TransactionHandler extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionHandler.class);

    private Coordinator coordinator;
    private TransactionMetadataManager transactionMetadataManager;
    private ProducerIdManager producerIdManager;
    private TransactionSynchronizer transactionSynchronizer;
    private NameService nameService;

    public TransactionHandler(Coordinator coordinator, TransactionMetadataManager transactionMetadataManager, ProducerIdManager producerIdManager,
                              TransactionSynchronizer transactionSynchronizer, NameService nameService) {
        this.coordinator = coordinator;
        this.transactionMetadataManager = transactionMetadataManager;
        this.producerIdManager = producerIdManager;
        this.transactionSynchronizer = transactionSynchronizer;
        this.nameService = nameService;
    }

    public TransactionMetadata initProducer(String clientId, String transactionId, int transactionTimeout) {
        checkCoordinatorState(clientId, transactionId);

        TransactionMetadata transactionMetadata = transactionMetadataManager.getTransaction(transactionId);
        if (transactionMetadata == null) {
            transactionMetadata = transactionMetadataManager.getOrCreateTransaction(new TransactionMetadata(transactionId, clientId,
                    producerIdManager.generateId(), transactionTimeout, SystemClock.now()));
        }

        if (transactionMetadata.isPrepared()) {
            throw new TransactionException(KafkaErrorCode.CONCURRENT_TRANSACTIONS.getCode());
        }

        synchronized (transactionMetadata) {
            return doInitProducer(transactionMetadata, transactionTimeout);
        }
    }

    protected TransactionMetadata doInitProducer(TransactionMetadata transactionMetadata, int transactionTimeout) {
        if (!transactionMetadata.getState().equals(TransactionState.EMPTY)) {
            tryAbort(transactionMetadata);
        }
        transactionMetadata.transitionStateTo(TransactionState.EMPTY);
        transactionMetadata.clear();
        transactionMetadata.nextProducerEpoch();
        transactionMetadata.setTimeout(transactionTimeout);
        transactionMetadata.setCreateTime(SystemClock.now());
        transactionMetadata.updateLastTime();
        return transactionMetadata;
    }

    protected void tryAbort(TransactionMetadata transactionMetadata) {
        try {
            doAbort(transactionMetadata);
            transactionMetadata.clearPrepare();
            transactionMetadata.clearOffsets();
        } catch (Exception e) {
            logger.error("initProducer abort exception, metadata: {}", transactionMetadata, e);
        }
    }

    public Map<String, List<PartitionMetadataAndError>> addPartitionsToTxn(String clientId, String transactionId, long producerId, short producerEpoch, Map<String, List<Integer>> partitions) {
        checkCoordinatorState(clientId, transactionId);

        TransactionMetadata transactionMetadata = transactionMetadataManager.getTransaction(transactionId);
        if (transactionMetadata == null || transactionMetadata.getProducerId() != producerId || !StringUtils.equals(transactionMetadata.getApp(), clientId)) {
            throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_ID_MAPPING.getCode());
        }
        if (transactionMetadata.getProducerEpoch() != producerEpoch) {
            throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_EPOCH.getCode());
        }
        if (transactionMetadata.isExpired()) {
            throw new TransactionException(KafkaErrorCode.INVALID_TRANSACTION_TIMEOUT.getCode());
        }
        if (!transactionMetadata.getState().equals(TransactionState.EMPTY) && !transactionMetadata.getState().equals(TransactionState.ONGOING)) {
            throw new TransactionException(KafkaErrorCode.INVALID_TXN_STATE.getCode());
        }
        if (transactionMetadata.isPrepared()) {
            throw new TransactionException(KafkaErrorCode.CONCURRENT_TRANSACTIONS.getCode());
        }

        synchronized (transactionMetadata) {
            return doAddPartitionsToTxn(transactionMetadata, partitions);
        }
    }

    protected Map<String, List<PartitionMetadataAndError>> doAddPartitionsToTxn(TransactionMetadata transactionMetadata, Map<String, List<Integer>> partitions) {
        transactionMetadata.transitionStateTo(TransactionState.ONGOING);
        transactionMetadata.updateLastTime();

        Map<String, List<PartitionMetadataAndError>> result = Maps.newHashMapWithExpectedSize(partitions.size());
        for (Map.Entry<String, List<Integer>> entry : partitions.entrySet()) {
            List<PartitionMetadataAndError> partitionMetadataAndErrors = Lists.newArrayListWithCapacity(entry.getValue().size());
            TopicName topic = TopicName.parse(entry.getKey());
            TopicConfig topicConfig = nameService.getTopicConfig(topic);

            for (Integer partition : entry.getValue()) {
                PartitionGroup partitionGroup = null;
                if (topicConfig != null) {
                    partitionGroup = topicConfig.fetchPartitionGroupByPartition((short) partition.intValue());
                }
                if (partitionGroup == null || partitionGroup.getLeaderBroker() == null) {
                    partitionMetadataAndErrors.add(new PartitionMetadataAndError(partition, KafkaErrorCode.UNKNOWN_TOPIC_OR_PARTITION.getCode()));
                } else {
                    try {
                        if (!transactionMetadata.containsPrepare(topic.getFullName(), (short) partition.intValue())) {
                            doPrepare(transactionMetadata, topic, (short) partition.intValue(), partitionGroup);
                        }
                        partitionMetadataAndErrors.add(new PartitionMetadataAndError(partition, KafkaErrorCode.NONE.getCode()));
                    } catch (Exception e) {
                        logger.error("transaction prepare exception, metadata:{}", transactionMetadata, e);
                        partitionMetadataAndErrors.add(new PartitionMetadataAndError(partition, KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode()));
                    }
                }
            }
            result.put(entry.getKey(), partitionMetadataAndErrors);
        }
        return result;
    }

    protected boolean doPrepare(TransactionMetadata transactionMetadata, TopicName topic, short partition, PartitionGroup partitionGroup) throws Exception {
        Broker broker = partitionGroup.getLeaderBroker();
        TransactionPrepare prepare = new TransactionPrepare(topic.getFullName(), partition, transactionMetadata.getApp(), broker.getId(), broker.getIp(), broker.getBackEndPort(),
                transactionMetadata.getId(), transactionMetadata.getProducerId(), transactionMetadata.getProducerEpoch(), transactionMetadata.getTimeout(), SystemClock.now());

        // TODO 批量优化
        transactionMetadata.addPrepare(prepare);
        return transactionSynchronizer.prepare(transactionMetadata, Sets.newHashSet(prepare));
    }

    public boolean endTxn(String clientId, String transactionId, long producerId, short producerEpoch, boolean isCommit) {
        checkCoordinatorState(clientId, transactionId);

        TransactionMetadata transactionMetadata = transactionMetadataManager.getTransaction(transactionId);
        if (transactionMetadata == null || transactionMetadata.getProducerId() != producerId || !StringUtils.equals(transactionMetadata.getApp(), clientId)) {
            throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_ID_MAPPING.getCode());
        }
        if (transactionMetadata.getProducerEpoch() != producerEpoch) {
            throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_EPOCH.getCode());
        }
        if (transactionMetadata.isExpired()) {
            throw new TransactionException(KafkaErrorCode.INVALID_TRANSACTION_TIMEOUT.getCode());
        }
        if (transactionMetadata.isPrepared()) {
            throw new TransactionException(KafkaErrorCode.CONCURRENT_TRANSACTIONS.getCode());
        }

        synchronized (transactionMetadata) {
            return doEndTxn(transactionMetadata, isCommit);
        }
    }

    protected boolean doEndTxn(TransactionMetadata transactionMetadata, boolean isCommit) {
        try {
            if (isCommit) {
                doCommit(transactionMetadata);
            } else {
                doAbort(transactionMetadata);
            }
            return true;
        } catch (Exception e) {
            logger.error("endTxn exception, metadata: {}, isCommit: {}", transactionMetadata, isCommit, e);
            throw new TransactionException(e, KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode());
        } finally {
            transactionMetadata.transitionStateTo(TransactionState.EMPTY);
            transactionMetadata.clear();
        }
    }

    protected void doCommit(TransactionMetadata transactionMetadata) throws Exception {
        transactionMetadata.transitionStateTo(TransactionState.PREPARE_COMMIT);
        transactionSynchronizer.prepareCommit(transactionMetadata, transactionMetadata.getPrepare());
        transactionSynchronizer.commit(transactionMetadata, transactionMetadata.getPrepare(), transactionMetadata.getOffsets());
        transactionMetadata.transitionStateTo(TransactionState.COMPLETE_COMMIT);
    }

    protected void doAbort(TransactionMetadata transactionMetadata) throws Exception {
        transactionMetadata.transitionStateTo(TransactionState.PREPARE_ABORT);
        transactionSynchronizer.prepareAbort(transactionMetadata, transactionMetadata.getPrepare());
        transactionSynchronizer.abort(transactionMetadata, transactionMetadata.getPrepare());
        transactionMetadata.transitionStateTo(TransactionState.COMPLETE_ABORT);
    }

    protected void checkCoordinatorState(String clientId, String transactionId) {
        if (!isStarted()) {
            throw new TransactionException(KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode());
        }
        if (!coordinator.isCurrentTransaction(clientId)) {
            throw new TransactionException(KafkaErrorCode.NOT_COORDINATOR.getCode());
        }
    }
}