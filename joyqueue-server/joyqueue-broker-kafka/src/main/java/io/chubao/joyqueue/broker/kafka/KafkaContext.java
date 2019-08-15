package io.chubao.joyqueue.broker.kafka;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.kafka.config.KafkaConfig;
import io.chubao.joyqueue.broker.kafka.coordinator.group.GroupCoordinator;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.ProducerSequenceManager;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.TransactionCoordinator;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.TransactionIdManager;

/**
 * KafkaContext
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/7
 */
public class KafkaContext {

    private KafkaConfig config;
    private GroupCoordinator groupCoordinator;
    private TransactionCoordinator transactionCoordinator;
    private TransactionIdManager transactionIdManager;
    private ProducerSequenceManager producerSequenceManager;
    private BrokerContext brokerContext;

    public KafkaContext(KafkaConfig config, GroupCoordinator groupCoordinator, TransactionCoordinator transactionCoordinator, TransactionIdManager transactionIdManager,
                        ProducerSequenceManager producerSequenceManager, BrokerContext brokerContext) {
        this.config = config;
        this.groupCoordinator = groupCoordinator;
        this.transactionCoordinator = transactionCoordinator;
        this.transactionIdManager = transactionIdManager;
        this.producerSequenceManager = producerSequenceManager;
        this.brokerContext = brokerContext;
    }

    public KafkaConfig getConfig() {
        return config;
    }

    public GroupCoordinator getGroupCoordinator() {
        return groupCoordinator;
    }

    public TransactionCoordinator getTransactionCoordinator() {
        return transactionCoordinator;
    }

    public TransactionIdManager getTransactionIdManager() {
        return transactionIdManager;
    }

    public ProducerSequenceManager getProducerSequenceManager() {
        return producerSequenceManager;
    }

    public BrokerContext getBrokerContext() {
        return brokerContext;
    }
}