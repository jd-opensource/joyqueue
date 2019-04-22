package com.jd.journalq.broker.kafka.coordinator.transaction.completion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TransactionCompletionThread
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/22
 */
public class TransactionCompletionThread implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionCompletionThread.class);

    private TransactionCompletionHandler transactionCompletionHandler;

    public TransactionCompletionThread(TransactionCompletionHandler transactionCompletionHandler) {
        this.transactionCompletionHandler = transactionCompletionHandler;
    }

    @Override
    public void run() {
        transactionCompletionHandler.handle();
    }
}