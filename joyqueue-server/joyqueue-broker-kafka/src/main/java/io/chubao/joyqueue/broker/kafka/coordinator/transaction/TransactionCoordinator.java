package io.chubao.joyqueue.broker.kafka.coordinator.transaction;

import io.chubao.joyqueue.broker.kafka.coordinator.Coordinator;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import io.chubao.joyqueue.broker.kafka.model.OffsetAndMetadata;
import io.chubao.joyqueue.broker.kafka.model.PartitionMetadataAndError;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.toolkit.service.Service;

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
    private TransactionOffsetHandler transactionOffsetHandler;

    public TransactionCoordinator(Coordinator coordinator, TransactionMetadataManager transactionMetadataManager,
                                  TransactionHandler transactionHandler, TransactionOffsetHandler transactionOffsetHandler) {
        this.coordinator = coordinator;
        this.transactionMetadataManager = transactionMetadataManager;
        this.transactionHandler = transactionHandler;
        this.transactionOffsetHandler = transactionOffsetHandler;
    }

    public Broker findCoordinator(String transactionId) {
        return coordinator.findTransaction(transactionId);
    }

    public boolean isCurrentCoordinator(String transactionId) {
        return coordinator.isCurrentTransaction(transactionId);
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

    public boolean handleAddOffsetsToTxn(String clientId, String transactionId, String groupId, long producerId, short producerEpoch) {
        return transactionOffsetHandler.addOffsetsToTxn(clientId, transactionId, groupId, producerId, producerEpoch);
    }

    public Map<String, List<PartitionMetadataAndError>> handleCommitOffset(String clientId, String transactionId, String groupId,
                                                                           long producerId, short producerEpoch, Map<String, List<OffsetAndMetadata>> offsetAndMetadata) {
        return transactionOffsetHandler.commitOffset(clientId, transactionId, groupId, producerId, producerEpoch, offsetAndMetadata);
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