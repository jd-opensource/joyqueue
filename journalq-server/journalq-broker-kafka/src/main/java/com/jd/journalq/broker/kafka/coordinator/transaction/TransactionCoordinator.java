package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.jd.journalq.broker.kafka.coordinator.Coordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import com.jd.journalq.broker.kafka.model.PartitionMetadataAndError;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.toolkit.service.Service;

import java.util.List;
import java.util.Map;

/**
 * TransactionCoordinator
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/10
 */
public class TransactionCoordinator extends Service {

    private Coordinator coordinator;
    private TransactionMetadataManager transactionMetadataManager;
    private TransactionHandler transactionHandler;

    public TransactionCoordinator(Coordinator coordinator, TransactionMetadataManager transactionMetadataManager, TransactionHandler transactionHandler) {
        this.coordinator = coordinator;
        this.transactionMetadataManager = transactionMetadataManager;
        this.transactionHandler = transactionHandler;
    }

    public Broker findCoordinator(String transactionId) {
        return coordinator.findTransactionCoordinator(transactionId);
    }

    public boolean isCurrentCoordinator(String transactionId) {
        return coordinator.isCurrentTransactionCoordinator(transactionId);
    }

    public TransactionMetadata handleInitProducer(String clientId, String transactionId, int transactionTimeout) {
        return transactionHandler.initProducer(clientId, transactionId, transactionTimeout);
    }

    public Map<String, List<PartitionMetadataAndError>> handleAddPartitionsToTxn(String clientId, String transactionId, long producerId, short producerEpoch, Map<String, List<Integer>> partitions) {
        return transactionHandler.addPartitionsToTxn(clientId, transactionId, producerId, producerEpoch, partitions);
    }

    public boolean handleEndTxn(String clientId, String transactionId, long producerId, short producerEpoch, boolean isCommit) {
        return transactionHandler.endTxn(clientId, transactionId, producerId, producerEpoch, isCommit);
    }

    public TransactionMetadata getTransaction(String transactionId) {
        return transactionMetadataManager.getTransaction(transactionId);
    }

    public boolean removeTransaction(TransactionMetadata transaction) {
        return transactionMetadataManager.removeTransaction(transaction.getId());
    }

    public boolean removeTransaction(String transactionId) {
        return transactionMetadataManager.removeTransaction(transactionId);
    }
}