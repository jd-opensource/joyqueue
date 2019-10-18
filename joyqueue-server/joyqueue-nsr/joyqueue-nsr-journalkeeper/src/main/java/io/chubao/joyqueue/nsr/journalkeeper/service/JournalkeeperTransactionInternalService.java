package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.nsr.journalkeeper.BatchOperationContext;
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
        BatchOperationContext.begin();
    }

    @Override
    public void commit() {
        BatchOperationContext.commit();
    }

    @Override
    public void rollback() {
        try {
            BatchOperationContext.rollback();
        } catch (Exception e) {
            logger.error("transaction rollback error", e);
        }
    }
}