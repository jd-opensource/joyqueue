package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.coordinator.Coordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionState;
import com.jd.journalq.broker.kafka.coordinator.transaction.exception.TransactionException;
import com.jd.journalq.broker.kafka.model.OffsetAndMetadata;
import com.jd.journalq.broker.kafka.model.PartitionMetadataAndError;
import com.jd.journalq.toolkit.service.Service;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * TransactionOffsetHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/16
 */
public class TransactionOffsetHandler extends Service {

    private Coordinator coordinator;
    private TransactionMetadataManager transactionMetadataManager;
    private TransactionSynchronizer transactionSynchronizer;

    public TransactionOffsetHandler(Coordinator coordinator, TransactionMetadataManager transactionMetadataManager, TransactionSynchronizer transactionSynchronizer) {
        this.coordinator = coordinator;
        this.transactionMetadataManager = transactionMetadataManager;
        this.transactionSynchronizer = transactionSynchronizer;
    }

    // 什么都不用做，真正提交时处理
    public boolean addOffsetsToTxn(String clientId, String transactionId, String groupId, long producerId, short producerEpoch) {
        checkCoordinatorState(clientId, transactionId);

        TransactionMetadata transactionMetadata = transactionMetadataManager.getTransaction(transactionId);
        if (transactionMetadata == null || transactionMetadata.getProducerId() != producerId || !StringUtils.equals(transactionMetadata.getApp(), clientId)) {
            throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_ID_MAPPING.getCode());
        }
        if (transactionMetadata.getProducerEpoch() != producerEpoch) {
            throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_EPOCH.getCode());
        }
        if (transactionMetadata.getState().equals(TransactionState.PREPARE_ABORT) || transactionMetadata.getState().equals(TransactionState.PREPARE_COMMIT)) {
            throw new TransactionException(KafkaErrorCode.CONCURRENT_TRANSACTIONS.getCode());
        }
        return true;
    }

    public Map<String, List<PartitionMetadataAndError>> commitOffset(String clientId, String transactionId, String groupId, long producerId, short producerEpoch, Map<String, List<OffsetAndMetadata>> offsetAndMetadata) {
        checkCoordinatorState(clientId, transactionId);

        TransactionMetadata transactionMetadata = transactionMetadataManager.getTransaction(transactionId);
        if (transactionMetadata == null || transactionMetadata.getProducerId() != producerId || !StringUtils.equals(transactionMetadata.getApp(), clientId)) {
            throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_ID_MAPPING.getCode());
        }
        if (transactionMetadata.getProducerEpoch() != producerEpoch) {
            throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_EPOCH.getCode());
        }
        if (transactionMetadata.getState().equals(TransactionState.PREPARE_ABORT) || transactionMetadata.getState().equals(TransactionState.PREPARE_COMMIT)) {
            throw new TransactionException(KafkaErrorCode.CONCURRENT_TRANSACTIONS.getCode());
        }

        synchronized (transactionMetadata) {
            return doCommitOffset(clientId, transactionId, groupId, producerId, producerEpoch, offsetAndMetadata);
        }
    }

    protected Map<String, List<PartitionMetadataAndError>> doCommitOffset(String clientId, String transactionId, String groupId, long producerId, short producerEpoch, Map<String, List<OffsetAndMetadata>> partitions) {
        checkCoordinatorState(clientId, transactionId);

        Map<String, List<PartitionMetadataAndError>> result = Maps.newHashMapWithExpectedSize(partitions.size());
        for (Map.Entry<String, List<OffsetAndMetadata>> entry : partitions.entrySet()) {
            List<PartitionMetadataAndError> partitionMetadataAndErrors = Lists.newArrayListWithCapacity(entry.getValue().size());
            for (OffsetAndMetadata offsetAndMetadata : entry.getValue()) {
                partitionMetadataAndErrors.add(new PartitionMetadataAndError(offsetAndMetadata.getPartition(), (short) 0));
            }
            result.put(entry.getKey(), partitionMetadataAndErrors);
        }
        return result;
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