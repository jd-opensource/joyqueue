package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.jd.journalq.broker.kafka.coordinator.Coordinator;
import com.jd.journalq.toolkit.service.Service;

/**
 * TransactionCompensator
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/15
 */
public class TransactionCompensator extends Service {

    private Coordinator coordinator;
    private TransactionLog transactionLog;
    private TransactionSynchronizer transactionSynchronizer;

    public TransactionCompensator(Coordinator coordinator, TransactionLog transactionLog, TransactionSynchronizer transactionSynchronizer) {
        this.coordinator = coordinator;
        this.transactionLog = transactionLog;
        this.transactionSynchronizer = transactionSynchronizer;
    }

    @Override
    protected void doStart() throws Exception {
    }

    @Override
    protected void doStop() {
    }
}
