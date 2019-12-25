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

/**
 * TransactionMarker
 *
 * author: gaohaoxiang
 * date: 2019/4/15
 */
public class TransactionMarker extends TransactionDomain {

    private String app;
    private String transactionId;
    private long producerId;
    private short producerEpoch;
    private short epoch;
    private TransactionState state;
    private int timeout;
    private long createTime;

    public TransactionMarker() {

    }

    public TransactionMarker(String app, String transactionId, long producerId, short producerEpoch, short epoch, TransactionState state, int timeout, long createTime) {
        this.app = app;
        this.transactionId = transactionId;
        this.producerId = producerId;
        this.producerEpoch = producerEpoch;
        this.epoch = epoch;
        this.state = state;
        this.timeout = timeout;
        this.createTime = createTime;
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

    public TransactionState getState() {
        return state;
    }

    public void setState(TransactionState state) {
        this.state = state;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "TransactionMarker{" +
                "app='" + app + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", producerId=" + producerId +
                ", producerEpoch=" + producerEpoch +
                ", state=" + state +
                ", timeout=" + timeout +
                ", createTime=" + createTime +
                '}';
    }
}