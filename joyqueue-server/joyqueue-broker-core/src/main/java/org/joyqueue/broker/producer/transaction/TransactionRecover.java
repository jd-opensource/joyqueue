/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.producer.transaction;

import org.joyqueue.broker.buffer.Serializer;
import org.joyqueue.broker.producer.ProduceConfig;
import org.joyqueue.message.BrokerPrepare;
import org.joyqueue.network.session.TransactionId;
import org.joyqueue.store.StoreService;
import org.joyqueue.store.transaction.TransactionStore;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

/**
 * TransactionRecover
 *
 * author: gaohaoxiang
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

            if (readIterator == null || !readIterator.hasNext()) {
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