package com.jd.journalq.broker.kafka.coordinator.transaction.completion;

import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.toolkit.concurrent.NamedThreadFactory;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TransactionCompletionScheduler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/15
 */
public class TransactionCompletionScheduler extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionCompletionScheduler.class);

    private KafkaConfig config;
    private TransactionCompletionHandler transactionCompletionHandler;

    private ScheduledExecutorService executor;

    public TransactionCompletionScheduler(KafkaConfig config, TransactionCompletionHandler transactionCompletionHandler) {
        this.config = config;
        this.transactionCompletionHandler = transactionCompletionHandler;
    }

    @Override
    protected void validate() throws Exception {
        executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("journalq-transaction-compensate"));
    }

    @Override
    protected void doStart() throws Exception {
        executor.scheduleAtFixedRate(new TransactionCompletionThread(transactionCompletionHandler),
                config.getTransactionLogInterval(), config.getTransactionLogInterval(), TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doStop() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}
