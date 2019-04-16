package com.jd.journalq.broker.kafka.coordinator.transaction.domain;

/**
 * TransactionOffset
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/16
 */
public class TransactionOffset {

    private String topic;
    private short partition;
    private long offset;
    private String app;
    private String transactionId;
    private long producerId;
    private short producerEpoch;
    private int timeout;
    private long createTime;

    public TransactionOffset() {

    }

    public TransactionOffset(String topic, short partition, long offset, String app, String transactionId, long producerId, short producerEpoch, int timeout, long createTime) {
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.app = app;
        this.transactionId = transactionId;
        this.producerId = producerId;
        this.producerEpoch = producerEpoch;
        this.timeout = timeout;
        this.createTime = createTime;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
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

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "TransactionOffset{" +
                "topic='" + topic + '\'' +
                ", partition=" + partition +
                ", offset=" + offset +
                ", app='" + app + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", producerId=" + producerId +
                ", producerEpoch=" + producerEpoch +
                ", timeout=" + timeout +
                ", createTime=" + createTime +
                '}';
    }
}