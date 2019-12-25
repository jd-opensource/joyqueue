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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.broker.producer.ProduceConfig;
import org.joyqueue.network.session.Producer;
import org.joyqueue.network.session.TransactionId;
import org.joyqueue.toolkit.time.SystemClock;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * UnCompletedTransactionManager
 *
 * author: gaohaoxiang
 * date: 2019/1/2
 */
public class UnCompletedTransactionManager {

    private ProduceConfig config;

    private ConcurrentMap<String /** app **/, ConcurrentMap<String /** topic **/, ConcurrentMap<String /** txId **/, TransactionId>>> unCompleteTransaction = Maps.newConcurrentMap();

    public UnCompletedTransactionManager(ProduceConfig config) {
        this.config = config;
    }

    public boolean putTransaction(TransactionId transactionId) {
        return getOrCreateTransactionMap(transactionId.getApp(), transactionId.getTopic()).put(transactionId.getTxId(), transactionId) == null;
    }

    public boolean removeTransaction(TransactionId transactionId) {
        return removeTransaction(transactionId.getTopic(), transactionId.getApp(), transactionId.getTxId());
    }

    public boolean removeTransaction(String topic, String app, String txId) {
        return getOrCreateTransactionMap(app, topic).remove(txId) != null;
    }

    public TransactionId getTransaction(String topic, String app, String txId) {
        return getOrCreateTransactionMap(app, topic).get(txId);
    }

    public int getTransactionCount(String topic, String app) {
        return getOrCreateTransactionMap(app, topic).size();
    }

    public List<TransactionId> getFeedback(Producer producer, int count) {
        ConcurrentMap<String, TransactionId> transactionMap = getOrCreateTransactionMap(producer.getApp(), producer.getTopic());
        List<TransactionId> result = Lists.newArrayListWithCapacity(count);
        long now = SystemClock.now();
        int index = 0;
        for (Map.Entry<String, TransactionId> entry : transactionMap.entrySet()) {
            TransactionId transactionId = entry.getValue();

            // 不需要补偿或未超时
            if (!transactionId.isFeedback() || !transactionId.isTimeout()) {
                continue;
            }

            long lastQueryTimestamp = transactionId.getLastQueryTimestamp();

            // 未到补偿时间
            if (config.getFeedbackTimeout() > now - lastQueryTimestamp) {
                continue;
            }

            // 更新查询时间
            if (!transactionId.setLastQueryTimestamp(lastQueryTimestamp, now)) {
                continue;
            }
            result.add(transactionId);
            index++;

            if (index >= count) {
                break;
            }
        }
        return result;
    }

    public ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String, TransactionId>>> getTransactions() {
        return unCompleteTransaction;
    }

    protected ConcurrentMap<String, TransactionId> getOrCreateTransactionMap(String app, String topic) {
        ConcurrentMap<String, ConcurrentMap<String, TransactionId>> topicMap = unCompleteTransaction.get(app);
        if (topicMap == null) {
            topicMap = Maps.newConcurrentMap();
            if (unCompleteTransaction.putIfAbsent(app, topicMap) != null) {
                topicMap = unCompleteTransaction.get(app);
            }
        }
        ConcurrentMap<String, TransactionId> transactionMap = topicMap.get(topic);
        if (transactionMap == null) {
            transactionMap = Maps.newConcurrentMap();
            if (topicMap.putIfAbsent(topic, transactionMap) != null) {
                transactionMap = topicMap.get(topic);
            }
        }
        return transactionMap;
    }
}