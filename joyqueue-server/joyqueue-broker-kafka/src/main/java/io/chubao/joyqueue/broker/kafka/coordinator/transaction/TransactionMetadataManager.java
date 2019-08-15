package io.chubao.joyqueue.broker.kafka.coordinator.transaction;

import io.chubao.joyqueue.broker.kafka.config.KafkaConfig;
import io.chubao.joyqueue.broker.kafka.coordinator.group.GroupMetadataManager;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * TransactionMetadataManager
 *
 * author: gaohaoxiang
 * date: 2019/4/10
 */
public class TransactionMetadataManager {

    protected static final Logger logger = LoggerFactory.getLogger(GroupMetadataManager.class);

    private KafkaConfig config;
    private io.chubao.joyqueue.broker.coordinator.transaction.TransactionMetadataManager transactionMetadataManager;

    public TransactionMetadataManager(KafkaConfig config, io.chubao.joyqueue.broker.coordinator.transaction.TransactionMetadataManager transactionMetadataManager) {
        this.config = config;
        this.transactionMetadataManager = transactionMetadataManager;
    }

    public TransactionMetadata tryGetTransaction(String transactionId) {
        return transactionMetadataManager.tryGetTransaction(transactionId);
    }

    public TransactionMetadata getTransaction(String transactionId) {
        return transactionMetadataManager.getTransaction(transactionId);
    }

    public TransactionMetadata getOrCreateTransaction(TransactionMetadata transaction) {
        return transactionMetadataManager.getOrCreateTransaction(transaction);
    }

    public List<TransactionMetadata> getTransactions() {
        return transactionMetadataManager.getTransactions();
    }

    public boolean removeTransaction(TransactionMetadata transaction) {
        return transactionMetadataManager.removeTransaction(transaction.getId());
    }

    public boolean removeTransaction(String transactionId) {
        return transactionMetadataManager.removeTransaction(transactionId);
    }

}