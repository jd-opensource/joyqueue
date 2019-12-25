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
package org.joyqueue.broker.kafka.coordinator.transaction.domain;

import java.util.Objects;

/**
 * TransactionOffset
 *
 * author: gaohaoxiang
 * date: 2019/4/16
 */
public class TransactionOffset extends TransactionDomain {

    private String topic;
    private short partition;
    private long offset;
    private String app;
    private String transactionId;
    private long producerId;
    private short producerEpoch;
    private short epoch;
    private int timeout;
    private long createTime;

    public TransactionOffset() {

    }

    public TransactionOffset(String topic, short partition, long offset, String app, String transactionId, long producerId, short producerEpoch, short epoch, int timeout, long createTime) {
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.app = app;
        this.transactionId = transactionId;
        this.producerId = producerId;
        this.producerEpoch = producerEpoch;
        this.epoch = epoch;
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

    public void setEpoch(short epoch) {
        this.epoch = epoch;
    }

    public short getEpoch() {
        return epoch;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionOffset that = (TransactionOffset) o;
        return partition == that.partition &&
                producerId == that.producerId &&
                producerEpoch == that.producerEpoch &&
                Objects.equals(topic, that.topic) &&
                Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(topic, partition, transactionId, producerId, producerEpoch);
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