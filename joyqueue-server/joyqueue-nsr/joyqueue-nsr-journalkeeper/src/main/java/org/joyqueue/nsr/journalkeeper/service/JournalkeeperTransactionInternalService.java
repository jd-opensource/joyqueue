package org.joyqueue.nsr.journalkeeper.service;

import org.joyqueue.nsr.journalkeeper.JournalkeeperBatchOperationContext;
import org.joyqueue.nsr.service.internal.TransactionInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JournalkeeperTransactionInternalService
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class JournalkeeperTransactionInternalService implements TransactionInternalService {

    protected static final Logger logger = LoggerFactory.getLogger(JournalkeeperTransactionInternalService.class);

    @Override
    public void begin() {
        JournalkeeperBatchOperationContext.begin();
    }

    @Override
    public void commit() {
        JournalkeeperBatchOperationContext.commit();
    }

    @Override
    public void rollback() {
        JournalkeeperBatchOperationContext.rollback();
    }
}