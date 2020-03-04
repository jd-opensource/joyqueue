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

import org.apache.commons.lang3.StringUtils;
import org.joyqueue.store.transaction.StoreTransactionId;
import org.joyqueue.toolkit.time.SystemClock;

import java.util.concurrent.atomic.AtomicLong;

/**
 *
 *
 * @author lining11
 * Date: 2018/8/23
 */
public class TransactionId {

    private String topic;
    private String app;
    private String txId;
    private String queryId;
    private StoreTransactionId storeId;
    private long timeout;
    private long startTime;
    private String producerId;
    private byte source;
    private short partition;

    private AtomicLong lastQueryTimestamp = new AtomicLong();

    public TransactionId(String topic, String app, String txId, String queryId, StoreTransactionId storeId, byte source, long timeout, long startTime, short partition) {
        this.topic = topic;
        this.app = app;
        this.txId = txId;
        this.queryId = queryId;
        this.storeId = storeId;
        this.source = source;
        this.timeout = timeout;
        this.startTime = startTime;
        this.partition = partition;
    }

    public boolean isFeedback() {
        return StringUtils.isNotBlank(queryId);
    }

    public boolean isTimeout() {
        return (SystemClock.now() > (startTime + timeout));
    }

    public boolean isExpired(long timeout) {
        return (SystemClock.now() > (startTime + timeout));
    }

    public String getTopic() {
        return topic;
    }

    public String getApp() {
        return app;
    }

    public String getTxId() {
        return txId;
    }

    public String getQueryId() {
        return queryId;
    }

    public StoreTransactionId getStoreId() {
        return storeId;
    }

    public long getTimeout() {
        return timeout;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getProducerId() {
        return producerId;
    }

    public long getLastQueryTimestamp() {
        return lastQueryTimestamp.get();
    }

    public boolean setLastQueryTimestamp(long oldLastQueryTimestamp, long lastQueryTimestamp) {
        return this.lastQueryTimestamp.compareAndSet(oldLastQueryTimestamp, lastQueryTimestamp);
    }

    public void setSource(byte source) {
        this.source = source;
    }

    public byte getSource() {
        return source;
    }

    public short getPartition() {
        return partition;
    }

    @Override
    public String toString() {
        return "TransactionId{" +
                "topic='" + topic + '\'' +
                ", app='" + app + '\'' +
                ", txId='" + txId + '\'' +
                ", queryId='" + queryId + '\'' +
                ", storeId=" + storeId +
                ", timeout=" + timeout +
                ", startTime=" + startTime +
                ", producerId='" + producerId + '\'' +
                ", source=" + source +
                ", partition=" + partition +
                ", lastQueryTimestamp=" + lastQueryTimestamp +
                '}';
    }
}