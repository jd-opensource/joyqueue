package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.coordinator.Coordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionState;
import com.jd.journalq.broker.kafka.coordinator.transaction.exception.TransactionException;
import com.jd.journalq.broker.kafka.model.PartitionMetadataAndError;
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

    private TransactionMetadataManager transactionMetadataManager;
    private Coordinator coordinator;
    private ProducerIdManager producerIdManager;
    private TransactionSynchronizer transactionSynchronizer;
    private NameService nameService;

    public TransactionHandler(TransactionMetadataManager transactionMetadataManager, Coordinator coordinator, ProducerIdManager producerIdManager,
                              TransactionSynchronizer transactionSynchronizer, NameService nameService) {
        this.transactionMetadataManager = transactionMetadataManager;
        this.coordinator = coordinator;
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

        synchronized (transactionMetadata) {
            return doInitProducer(transactionMetadata);
        }
    }

    protected TransactionMetadata doInitProducer(TransactionMetadata transactionMetadata) {
        transactionMetadata.transitionStateTo(TransactionState.EMPTY);
        transactionMetadata.nextProducerEpoch();
        return transactionMetadata;
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
        if (!transactionMetadata.getState().equals(TransactionState.EMPTY) && !transactionMetadata.getState().equals(TransactionState.ONGOING)) {
            throw new TransactionException(KafkaErrorCode.INVALID_TXN_STATE.getCode());
        }

        synchronized (transactionMetadata) {
            return doAddPartitionsToTxn(transactionMetadata, partitions);
        }
    }

    protected Map<String, List<PartitionMetadataAndError>> doAddPartitionsToTxn(TransactionMetadata transactionMetadata, Map<String, List<Integer>> partitions) {
        transactionMetadata.transitionStateTo(TransactionState.ONGOING);

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
                            transactionSynchronizer.prepare(transactionMetadata, topic.getFullName(), partition, partitionGroup.getLeaderBroker());
                        }
                        partitionMetadataAndErrors.add(new PartitionMetadataAndError(partition, KafkaErrorCode.NONE.getCode()));
                    } catch (Exception e) {
                        logger.error("transaction prepare exception, metadata:{}", transactionMetadata, e);
                        partitionMetadataAndErrors.add(new PartitionMetadataAndError(partition, KafkaErrorCode.exceptionFor(e)));
                    }
                }
            }
            result.put(entry.getKey(), partitionMetadataAndErrors);
        }
        return result;
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
        if (transactionMetadata.getState().equals(TransactionState.EMPTY)) {
            throw new TransactionException(KafkaErrorCode.INVALID_TXN_STATE.getCode());
        }
        if (transactionMetadata.getState().equals(TransactionState.PREPARE_COMMIT) || transactionMetadata.getState().equals(TransactionState.PREPARE_ABORT)) {
            throw new TransactionException(KafkaErrorCode.CONCURRENT_TRANSACTIONS.getCode());
        }

        synchronized (transactionMetadata) {
            return doEndTxn(transactionMetadata, isCommit);
        }
    }

    protected boolean doEndTxn(TransactionMetadata transactionMetadata, boolean isCommit) {
        try {
            if (isCommit) {
                transactionMetadata.transitionStateTo(TransactionState.PREPARE_COMMIT);
                transactionSynchronizer.commit(transactionMetadata);
                transactionMetadata.transitionStateTo(TransactionState.COMPLETE_COMMIT);
            } else {
                transactionMetadata.transitionStateTo(TransactionState.PREPARE_ABORT);
                transactionSynchronizer.abort(transactionMetadata);
                transactionMetadata.transitionStateTo(TransactionState.COMPLETE_ABORT);
            }
            return true;
        } catch (Exception e) {
            logger.error("endTxn exception, metadata: {}, isCommit: {}", transactionMetadata, isCommit, e);
            throw new TransactionException(e, KafkaErrorCode.exceptionFor(e));
        } finally {
            transactionMetadata.transitionStateTo(TransactionState.EMPTY);
        }
    }

    protected void checkCoordinatorState(String clientId, String transactionId) {
        if (!isStarted()) {
            throw new TransactionException(KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode());
        }
        if (!coordinator.isCurrentTransactionCoordinator(clientId)) {
            throw new TransactionException(KafkaErrorCode.NOT_COORDINATOR.getCode());
        }
    }
}