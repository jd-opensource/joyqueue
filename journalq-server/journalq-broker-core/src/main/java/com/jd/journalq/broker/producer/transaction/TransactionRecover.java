package com.jd.journalq.broker.producer.transaction;

import com.jd.journalq.broker.buffer.Serializer;
import com.jd.journalq.broker.producer.ProduceConfig;
import com.jd.journalq.message.BrokerPrepare;
import com.jd.journalq.network.session.TransactionId;
import com.jd.journalq.store.StoreService;
import com.jd.journalq.store.transaction.TransactionStore;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

/**
 * TransactionRecover
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/2
 */
public class TransactionRecover {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionRecover.class);

    private ProduceConfig config;
    private UnCompletedTransactionManager unCompletedTransactionManager;
    private StoreService store;

    public TransactionRecover(ProduceConfig config, UnCompletedTransactionManager unCompletedTransactionManager, StoreService store) {
        this.config = config;
        this.unCompletedTransactionManager = unCompletedTransactionManager;
        this.store = store;
    }

    public void recover() {
        List<TransactionStore> allTransactionStores = store.getAllTransactionStores();
        if (CollectionUtils.isEmpty(allTransactionStores)) {
            return;
        }
        for (TransactionStore transactionStore : allTransactionStores) {
            doRecover(transactionStore);
        }
    }

    protected void doRecover(TransactionStore transactionStore) {
        int[] storeIds = transactionStore.list();
        for (int storeId : storeIds) {
            doRecover(transactionStore, storeId);
        }
    }

    protected void doRecover(TransactionStore transactionStore, int storeId) {
        try {
            boolean isExpired = false;

            Iterator<ByteBuffer> readIterator = transactionStore.readIterator(storeId);

            if (!readIterator.hasNext()) {
                isExpired = true;
            } else {
                ByteBuffer byteBuffer = readIterator.next();
                BrokerPrepare brokerPrepare = Serializer.readBrokerPrepare(byteBuffer);
                TransactionId transactionId = new TransactionId(brokerPrepare.getTopic(), brokerPrepare.getApp(),
                        brokerPrepare.getTxId(), brokerPrepare.getQueryId(), storeId, brokerPrepare.getSource(),
                        brokerPrepare.getTimeout(), brokerPrepare.getStartTime());

                if (transactionId.isExpired(config.getTransactionExpireTime())) {
                    isExpired = true;
                    logger.info("recover transaction is expired, topic: {}, app: {}, txId: {}", brokerPrepare.getTopic(), brokerPrepare.getApp(), brokerPrepare.getTxId());
                } else {
                    unCompletedTransactionManager.putTransaction(transactionId);
                    logger.info("recover transaction, topic: {}, app: {}, txId: {}", brokerPrepare.getTopic(), brokerPrepare.getApp(), brokerPrepare.getTxId());
                }
            }

            if (isExpired) {
                transactionStore.remove(storeId);
            }
        } catch (Exception e) {
            logger.error("recover transaction exception, store: {}, storeId: {}", transactionStore, storeId, e);
        }
    }
}