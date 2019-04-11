package com.jd.journalq.broker.kafka.coordinator.transaction.domain;

import com.jd.journalq.toolkit.time.SystemClock;

import java.util.Objects;

/**
 * TransactionMetadata
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/10
 */
public class TransactionMetadata extends com.jd.journalq.broker.coordinator.transaction.domain.TransactionMetadata {

    private String clientId;
    private int timeout;
    private long producerId;
    private short producerEpoch = 0;
    private long createTime = SystemClock.now();
    private TransactionState state = TransactionState.EMPTY;

    public TransactionMetadata(String id) {
        super(id);
    }

    public TransactionMetadata(String id, String clientId, int timeout) {
        super(id);
        this.clientId = clientId;
        this.timeout = timeout;
    }

    public void transitionStateTo(TransactionState state) {
        this.state = state;
    }

    public boolean isExpired(long timeout) {
        return (SystemClock.now() > (createTime + timeout));
    }

    public boolean isExpired() {
        return isExpired(timeout);
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public void nextProducerEpoch() {
        this.producerEpoch++;
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

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setState(TransactionState state) {
        this.state = state;
    }

    public TransactionState getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMetadata that = (com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMetadata) o;
        return timeout == that.timeout &&
                producerId == that.producerId &&
                producerEpoch == that.producerEpoch &&
                Objects.equals(clientId, that.clientId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(clientId, timeout, producerId, producerEpoch);
    }

    @Override
    public String toString() {
        return "TransactionMetadata{" +
                "clientId='" + clientId + '\'' +
                ", timeout=" + timeout +
                ", producerId=" + producerId +
                ", producerEpoch=" + producerEpoch +
                '}';
    }
}