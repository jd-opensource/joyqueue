package com.jd.journalq.broker.coordinator.transaction;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.jd.journalq.broker.coordinator.config.CoordinatorConfig;
import com.jd.journalq.broker.coordinator.transaction.domain.TransactionMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * TransactionMetadataManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/10
 */
public class TransactionMetadataManager {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionMetadataManager.class);

    private String namespace;
    private CoordinatorConfig config;

    private Cache<String, TransactionMetadata> transactionCache;

    public TransactionMetadataManager(String namespace, CoordinatorConfig config) {
        this.namespace = namespace;
        this.config = config;
        this.transactionCache = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getTransactionExpireTime(), TimeUnit.MILLISECONDS)
                .maximumSize(config.getTransactionMaxNum())
                .build();
    }

    public <T extends TransactionMetadata> T getTransaction(String transactionId) {
        return (T) transactionCache.getIfPresent(transactionId);
    }

    public <T extends TransactionMetadata> List<T> getTransactions() {
        return (List<T>) Lists.newArrayList(transactionCache.asMap().values());
    }

    public <T extends TransactionMetadata> T getOrCreateTransaction(TransactionMetadata transaction) {
        return getOrCreateTransaction(transaction.getId(), new Callable<TransactionMetadata>() {
            @Override
            public TransactionMetadata call() throws Exception {
                return transaction;
            }
        });
    }

    public <T extends TransactionMetadata> T getOrCreateTransaction(String transactionId) {
        return getOrCreateTransaction(transactionId, new Callable<TransactionMetadata>() {
            @Override
            public TransactionMetadata call() throws Exception {
                return new TransactionMetadata(transactionId);
            }
        });
    }

    public <T extends TransactionMetadata> T getOrCreateTransaction(String transactionId, Callable<TransactionMetadata> callable) {
        try {
            return (T) transactionCache.get(transactionId, callable);
        } catch (ExecutionException e) {
            logger.error("getOrCreate coordinatorGroup exception, transactionId: {}", transactionId, e);
            return (T) transactionCache.getIfPresent(transactionId);
        }
    }

    public boolean removeTransaction(String transactionId) {
        transactionCache.invalidate(transactionId);
        return true;
    }
}