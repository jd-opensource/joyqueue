package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.nsr.journalkeeper.TransactionContext;
import io.chubao.joyqueue.nsr.service.internal.TransactionInternalService;

/**
 * JournalkeeperTransactionInternalService
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class JournalkeeperTransactionInternalService implements TransactionInternalService {

    @Override
    public void begin() {
        TransactionContext.beginTransaction();
    }

    @Override
    public void commit() {
        TransactionContext.commit();
    }

    @Override
    public void rollback() {
        TransactionContext.rollback();
    }
}