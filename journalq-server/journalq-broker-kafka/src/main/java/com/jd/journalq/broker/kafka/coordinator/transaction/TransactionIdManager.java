package com.jd.journalq.broker.kafka.coordinator.transaction;

/**
 * TransactionIdManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/12
 */
public class TransactionIdManager {

    public String generateId(String topic, String app, String transactionId, long producerId, short producerEpoch) {
        return String.format("%s_%s_%s_%s_%s", topic, app, transactionId, producerId, producerEpoch);
    }
}