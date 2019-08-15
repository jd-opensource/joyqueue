package io.chubao.joyqueue.broker.kafka.coordinator.transaction;

/**
 * TransactionIdManager
 *
 * author: gaohaoxiang
 * date: 2019/4/12
 */
public class TransactionIdManager {

    public String generateId(String topic, int partition, String app, String transactionId, long producerId, short producerEpoch) {
        return String.format("%s_%s_%s_%s_%s_%s", topic, partition, app, transactionId, producerId, producerEpoch);
    }
}