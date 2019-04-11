package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.message.KafkaBrokerMessage;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangkepeng on 16-7-27.
 */
public class ProduceRequest extends KafkaRequestOrResponse {

    private short requiredAcks;
    private int ackTimeoutMs;
    private String transactionalId;
    private Map<String, List<PartitionRequest>> partitionRequests;

    private boolean transaction = false;
    private int partitionNum;
    private long producerId;
    private short producerEpoch;

    public short getRequiredAcks() {
        return requiredAcks;
    }

    public void setRequiredAcks(short requiredAcks) {
        this.requiredAcks = requiredAcks;
    }

    public void setAckTimeoutMs(int ackTimeoutMs) {
        this.ackTimeoutMs = ackTimeoutMs;
    }

    public int getAckTimeoutMs() {
        return ackTimeoutMs;
    }

    public String getTransactionalId() {
        return transactionalId;
    }

    public void setTransactionalId(String transactionalId) {
        this.transactionalId = transactionalId;
    }

    public void setPartitionRequests(Map<String, List<PartitionRequest>> partitionRequests) {
        this.partitionRequests = partitionRequests;
    }

    public Map<String, List<PartitionRequest>> getPartitionRequests() {
        return partitionRequests;
    }

    public void setPartitionNum(int partitionNum) {
        this.partitionNum = partitionNum;
    }

    public int getPartitionNum() {
        return partitionNum;
    }

    public void setTransaction(boolean transaction) {
        this.transaction = transaction;
    }

    public boolean isTransaction() {
        return transaction;
    }

    public long getProducerId() {
        return producerId;
    }

    public void setProducerId(long producerId) {
        this.producerId = producerId;
    }

    public short getProducerEpoch() {
        return producerEpoch;
    }

    public void setProducerEpoch(short producerEpoch) {
        this.producerEpoch = producerEpoch;
    }

    @Override
    public int type() {
        return KafkaCommandType.PRODUCE.getCode();
    }

    @Override
    public String toString() {
        return describe();
    }

    private String describe() {
        StringBuilder producerRequest = new StringBuilder();
        producerRequest.append("Name: " + this.getClass().getSimpleName());
        producerRequest.append("; RequiredAcks: " + requiredAcks);
        producerRequest.append("; AckTimeoutMs: " + ackTimeoutMs + " ms");
        return producerRequest.toString();
    }

    public static class PartitionRequest {

        private int partition;
        private List<KafkaBrokerMessage> messages;

        public PartitionRequest() {

        }

        public PartitionRequest(int partition, List<KafkaBrokerMessage> messages) {
            this.partition = partition;
            this.messages = messages;
        }

        public void setPartition(int partition) {
            this.partition = partition;
        }

        public int getPartition() {
            return partition;
        }

        public void setMessages(List<KafkaBrokerMessage> messages) {
            this.messages = messages;
        }

        public List<KafkaBrokerMessage> getMessages() {
            return messages;
        }
    }
}