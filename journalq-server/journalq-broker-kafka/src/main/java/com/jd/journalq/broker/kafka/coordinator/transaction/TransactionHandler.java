package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.coordinator.Coordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionState;
import com.jd.journalq.broker.kafka.coordinator.transaction.exception.CoordinatorTransactionException;
import com.jd.journalq.broker.kafka.model.PartitionMetadataAndError;
import com.jd.journalq.toolkit.service.Service;
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

    public TransactionHandler(TransactionMetadataManager transactionMetadataManager, Coordinator coordinator, ProducerIdManager producerIdManager) {
        this.transactionMetadataManager = transactionMetadataManager;
        this.coordinator = coordinator;
        this.producerIdManager = producerIdManager;
    }

    public TransactionMetadata initProducer(String clientId, String transactionId, int transactionTimeout) {
        if (!isStarted()) {
            throw new CoordinatorTransactionException(KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode());
        }
        if (!coordinator.isCurrentTransactionCoordinator(clientId)) {
            throw new CoordinatorTransactionException(KafkaErrorCode.NOT_COORDINATOR.getCode());
        }

        TransactionMetadata transactionMetadata = transactionMetadataManager.getTransaction(transactionId);
        if (transactionMetadata == null) {
            transactionMetadata = transactionMetadataManager.getOrCreateTransaction(new TransactionMetadata(transactionId, clientId, transactionTimeout));
        }

        synchronized (transactionMetadata) {
            return doInitProducer(transactionMetadata);
        }
    }

    protected TransactionMetadata doInitProducer(TransactionMetadata coordinatorTransactionMetadata) {
        coordinatorTransactionMetadata.setProducerId(producerIdManager.generateId());
        coordinatorTransactionMetadata.nextProducerEpoch();
        return coordinatorTransactionMetadata;
    }

    // TODO 状态码处理
    // TODO 事务状态判断
    public Map<String, List<PartitionMetadataAndError>> addPartitionsToTxn(String clientId, String transactionId, long producerId, short producerEpoch, Map<String, List<Integer>> partitions) {
        if (!isStarted()) {
            throw new CoordinatorTransactionException(KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode());
        }
        if (!coordinator.isCurrentTransactionCoordinator(clientId)) {
            throw new CoordinatorTransactionException(KafkaErrorCode.NOT_COORDINATOR.getCode());
        }
        TransactionMetadata transactionMetadata = transactionMetadataManager.getTransaction(transactionId);
        if (transactionMetadata == null) {
            throw new CoordinatorTransactionException(KafkaErrorCode.INVALID_PRODUCER_ID_MAPPING.getCode());
        }
        if (transactionMetadata.getProducerId() != producerId || transactionMetadata.getProducerEpoch() != producerEpoch) {
            throw new CoordinatorTransactionException(KafkaErrorCode.INVALID_PRODUCER_EPOCH.getCode());
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
            for (Integer partition : entry.getValue()) {
                partitionMetadataAndErrors.add(new PartitionMetadataAndError(partition, KafkaErrorCode.NONE.getCode()));
            }
            result.put(entry.getKey(), partitionMetadataAndErrors);
        }
        return result;
    }

    public boolean endTxn(String clientId, String transactionId, long producerId, short producerEpoch, boolean isCommit) {
        if (!isStarted()) {
            throw new CoordinatorTransactionException(KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode());
        }
        if (!coordinator.isCurrentTransactionCoordinator(clientId)) {
            throw new CoordinatorTransactionException(KafkaErrorCode.NOT_COORDINATOR.getCode());
        }

        TransactionMetadata transactionMetadata = transactionMetadataManager.getTransaction(transactionId);
        if (transactionMetadata == null) {
            throw new CoordinatorTransactionException(KafkaErrorCode.INVALID_PRODUCER_ID_MAPPING.getCode());
        }
        if (transactionMetadata.getProducerId() != producerId || transactionMetadata.getProducerEpoch() != producerEpoch) {
            throw new CoordinatorTransactionException(KafkaErrorCode.INVALID_PRODUCER_EPOCH.getCode());
        }

        synchronized (transactionMetadata) {
            return doEndTxn(transactionMetadata, isCommit);
        }
    }

    protected boolean doEndTxn(TransactionMetadata transactionMetadata, boolean isCommit) {
        if (isCommit) {
            transactionMetadata.transitionStateTo(TransactionState.PREPARE_COMMIT);
        } else {
            transactionMetadata.transitionStateTo(TransactionState.PREPARE_ABORT);
        }
        return true;
    }
}