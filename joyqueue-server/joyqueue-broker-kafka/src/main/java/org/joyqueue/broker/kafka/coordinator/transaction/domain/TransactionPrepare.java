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
 * TransactionPrepare
 *
 * author: gaohaoxiang
 * date: 2019/4/12
 */
public class TransactionPrepare extends TransactionDomain {

    private String topic;
    private short partition;
    private String app;
    // 因为事务不会复制，临时记录
    private int brokerId;
    private String brokerHost;
    private int brokerPort;
    private String transactionId;
    private long producerId;
    private short producerEpoch;
    private short epoch;
    private int timeout;
    private long createTime;

    public TransactionPrepare() {

    }

    public TransactionPrepare(String topic, short partition, String app, int brokerId, String brokerHost, int brokerPort, String transactionId,
                              long producerId, short producerEpoch, short epoch, int timeout, long createTime) {
        this.topic = topic;
        this.partition = partition;
        this.app = app;
        this.brokerId = brokerId;
        this.brokerHost = brokerHost;
        this.brokerPort = brokerPort;
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

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }

    public int getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(int brokerId) {
        this.brokerId = brokerId;
    }

    public String getBrokerHost() {
        return brokerHost;
    }

    public void setBrokerHost(String brokerHost) {
        this.brokerHost = brokerHost;
    }

    public int getBrokerPort() {
        return brokerPort;
    }

    public void setBrokerPort(int brokerPort) {
        this.brokerPort = brokerPort;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setEpoch(short epoch) {
        this.epoch = epoch;
    }

    public short getEpoch() {
        return epoch;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
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
        TransactionPrepare that = (TransactionPrepare) o;
        return partition == that.partition &&
                producerId == that.producerId &&
                producerEpoch == that.producerEpoch &&
                Objects.equals(topic, that.topic) &&
                Objects.equals(app, that.app) &&
                Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(topic, partition, app, transactionId, producerId, producerEpoch);
    }

    @Override
    public String toString() {
        return "TransactionPrepare{" +
                "topic='" + topic + '\'' +
                ", partition=" + partition +
                ", app='" + app + '\'' +
                ", brokerId=" + brokerId +
                ", brokerHost='" + brokerHost + '\'' +
                ", brokerPort=" + brokerPort +
                ", transactionId='" + transactionId + '\'' +
                ", producerId=" + producerId +
                ", producerEpoch=" + producerEpoch +
                ", epoch=" + epoch +
                ", timeout=" + timeout +
                ", createTime=" + createTime +
                '}';
    }
}