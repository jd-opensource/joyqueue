package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.Coordinator;
import com.jd.journalq.toolkit.concurrent.NamedThreadFactory;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TransactionCompensator
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/15
 */
public class TransactionCompensator extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionCompensator.class);

    private KafkaConfig config;
    private Coordinator coordinator;
    private TransactionLog transactionLog;
    private TransactionSynchronizer transactionSynchronizer;

    private ScheduledExecutorService executor;

    public TransactionCompensator(KafkaConfig config, Coordinator coordinator, TransactionLog transactionLog, TransactionSynchronizer transactionSynchronizer) {
        this.config = config;
        this.coordinator = coordinator;
        this.transactionLog = transactionLog;
        this.transactionSynchronizer = transactionSynchronizer;
    }

    @Override
    protected void validate() throws Exception {
        executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("journalq-transaction-compensate"));
    }

    @Override
    protected void doStart() throws Exception {
        executor.scheduleAtFixedRate(new TransactionCompensateThread(config, coordinator, transactionLog, transactionSynchronizer),
                config.getTransactionLogInterval(), config.getTransactionLogInterval(), TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doStop() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}
