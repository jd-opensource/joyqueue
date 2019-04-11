package com.jd.journalq.broker.kafka;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.group.GroupCoordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionCoordinator;
import com.jd.journalq.broker.kafka.handler.ratelimit.KafkaRateLimitHandlerFactory;

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
    private KafkaRateLimitHandlerFactory rateLimitHandlerFactory;
    private BrokerContext brokerContext;

    public KafkaContext(KafkaConfig config, GroupCoordinator groupCoordinator, TransactionCoordinator transactionCoordinator,
                        KafkaRateLimitHandlerFactory rateLimitHandlerFactory, BrokerContext brokerContext) {
        this.config = config;
        this.groupCoordinator = groupCoordinator;
        this.transactionCoordinator = transactionCoordinator;
        this.rateLimitHandlerFactory = rateLimitHandlerFactory;
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

    public KafkaRateLimitHandlerFactory getRateLimitHandlerFactory() {
        return rateLimitHandlerFactory;
    }

    public BrokerContext getBrokerContext() {
        return brokerContext;
    }
}