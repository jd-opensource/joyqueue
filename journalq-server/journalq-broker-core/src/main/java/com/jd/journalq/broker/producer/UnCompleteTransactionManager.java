package com.jd.journalq.broker.producer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.common.network.session.Producer;
import com.jd.journalq.common.network.session.TransactionId;
import com.jd.journalq.toolkit.time.SystemClock;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * UnCompleteTransactionManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/2
 */
public class UnCompleteTransactionManager {

    private ProduceConfig config;

    private ConcurrentMap<String /** app **/, ConcurrentMap<String /** topic **/, ConcurrentMap<String /** txId **/, TransactionId>>> unCompleteTransaction = Maps.newConcurrentMap();

    public UnCompleteTransactionManager(ProduceConfig config) {
        this.config = config;
    }

    public boolean putTransaction(TransactionId transactionId) {
        return getOrCreateTransactionMap(transactionId.getApp(), transactionId.getTopic()).put(transactionId.getTxId(), transactionId) == null;
    }

    public boolean removeTransaction(String topic, String app, String txId) {
        return getOrCreateTransactionMap(app, topic).remove(txId) != null;
    }

    public TransactionId getTransaction(String topic, String app, String txId) {
        return getOrCreateTransactionMap(app, topic).get(txId);
    }

    public List<TransactionId> txFeedback(Producer producer, int count) {
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