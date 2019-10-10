package io.chubao.joyqueue.nsr.composition;

import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.service.internal.TransactionInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CompositionTransactionInternalService
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class CompositionTransactionInternalService implements TransactionInternalService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionTransactionInternalService.class);

    private CompositionConfig config;
    private TransactionInternalService igniteTransactionInternalService;
    private TransactionInternalService journalkeeperTransactionInternalService;

    public CompositionTransactionInternalService(CompositionConfig config, TransactionInternalService igniteTransactionInternalService,
                                                 TransactionInternalService journalkeeperTransactionInternalService) {
        this.config = config;
        this.igniteTransactionInternalService = igniteTransactionInternalService;
        this.journalkeeperTransactionInternalService = journalkeeperTransactionInternalService;
    }

    @Override
    public void begin() {
        if (config.isWriteIgnite()) {
            igniteTransactionInternalService.begin();
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTransactionInternalService.begin();
            } catch (Exception e) {
                logger.info("journalkeeper transaction begin exception", e);
            }
        }
    }

    @Override
    public void commit() {
        if (config.isWriteIgnite()) {
            igniteTransactionInternalService.commit();
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTransactionInternalService.commit();
            } catch (Exception e) {
                logger.info("journalkeeper transaction commit exception", e);
            }
        }
    }

    @Override
    public void rollback() {
        if (config.isWriteIgnite()) {
            igniteTransactionInternalService.rollback();
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTransactionInternalService.rollback();
            } catch (Exception e) {
                logger.info("journalkeeper transaction rollback exception", e);
            }
        }
    }
}