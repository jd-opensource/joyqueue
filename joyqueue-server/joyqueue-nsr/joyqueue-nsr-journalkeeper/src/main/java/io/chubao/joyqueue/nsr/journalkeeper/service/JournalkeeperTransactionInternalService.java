package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.nsr.journalkeeper.TransactionContext;
import io.chubao.joyqueue.nsr.service.internal.TransactionInternalService;
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
        TransactionContext.beginTransaction();
    }

    @Override
    public void commit() {
        TransactionContext.commit();
    }

    @Override
    public void rollback() {
        try {
            TransactionContext.rollback();
        } catch (Exception e) {
            logger.error("transaction rollback error", e);
        }
    }
}