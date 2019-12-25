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

import com.google.common.collect.Sets;
import org.joyqueue.toolkit.time.SystemClock;

import java.util.Objects;
import java.util.Set;

/**
 * TransactionMetadata
 *
 * author: gaohaoxiang
 * date: 2019/4/10
 */
public class TransactionMetadata extends org.joyqueue.broker.coordinator.transaction.domain.TransactionMetadata {

    private String app;
    private int timeout;
    private long producerId;
    private short producerEpoch = 0;
    private short epoch = 0;
    private long createTime;
    private long lastTime;
    private TransactionState state = TransactionState.EMPTY;
    private Set<TransactionPrepare> prepare;
    private Set<TransactionOffset> offsets;

    public TransactionMetadata() {

    }

    public TransactionMetadata(String id) {
        super(id);
    }

    public TransactionMetadata(String id, String app, long producerId, int timeout, long createTime) {
        super(id);
        this.app = app;
        this.timeout = timeout;
        this.producerId = producerId;
        this.createTime = createTime;
        this.lastTime = createTime;
    }

    public void addPrepare(TransactionPrepare transactionPrepare) {
        if (prepare == null) {
            prepare = Sets.newHashSet();
        }
        prepare.add(transactionPrepare);
    }

    public void addPrepare(Set<TransactionPrepare> transactionPrepare) {
        if (prepare == null) {
            prepare = Sets.newHashSet();
        }
        prepare.addAll(transactionPrepare);
    }

    public boolean containsPrepare(String topic, short partition) {
        return getPrepare(topic, partition) != null;
    }

    public TransactionPrepare getPrepare(String topic, short partition) {
        if (prepare == null) {
            return null;
        }
        for (TransactionPrepare transactionPrepare : prepare) {
            if (transactionPrepare.getTopic().equals(topic) && transactionPrepare.getPartition() == partition) {
                return transactionPrepare;
            }
        }
        return null;
    }

    public void clearPrepare() {
        if (prepare == null) {
            return;
        }
        prepare.clear();
    }

    public void addOffsets(Set<TransactionOffset> offsets) {
        if (this.offsets == null) {
            this.offsets = Sets.newHashSet();
        }
        this.offsets.remove(offsets);
        this.offsets.addAll(offsets);
    }

    public void addOffset(TransactionOffset offset) {
        if (this.offsets == null) {
            this.offsets = Sets.newHashSet();
        }
        this.offsets.remove(offset);
        this.offsets.add(offset);
    }

    public boolean containsOffset(String topic, short partition) {
        return getOffest(topic, partition) != null;
    }

    public TransactionOffset getOffest(String topic, short partition) {
        if (offsets == null) {
            return null;
        }
        for (TransactionOffset offset : offsets) {
            if (offset.getTopic().equals(topic) && offset.getPartition() == partition) {
                return offset;
            }
        }
        return null;
    }

    public void clearOffsets() {
        if (offsets == null) {
            return;
        }
        offsets.clear();
    }

    public void clear() {
        clearPrepare();
        clearOffsets();
    }

    public void updateLastTime() {
        this.lastTime = SystemClock.now();
    }

    public void transitionStateTo(TransactionState state) {
        this.state = state;
    }

    public boolean isExpired(long timeout) {
        return isExpired(SystemClock.now(), timeout);
    }

    public boolean isExpired(long base, long timeout) {
        return (base > (lastTime + timeout));
    }

    public boolean isExpired() {
        return isExpired(timeout);
    }

    public void nextProducerEpoch() {
        this.producerEpoch++;
    }

    public void nextEpoch() {
        this.epoch++;
    }

    public boolean isCompleted() {
        return state.equals(TransactionState.COMPLETE_ABORT) || state.equals(TransactionState.COMPLETE_COMMIT) || state.equals(TransactionState.DEAD);
    }

    public boolean isPrepared() {
        return state.equals(TransactionState.PREPARE_ABORT) || state.equals(TransactionState.PREPARE_COMMIT);
    }

    @Override
    public String getExtension() {
        return String.format("{timeout: '%s', producerId: '%s', producerEpoch: '%s', createTime: '%s', state: '%s', prepare: '%s'}",
                timeout, producerId, producerEpoch, createTime, state, prepare);
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
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

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setState(TransactionState state) {
        this.state = state;
    }

    public TransactionState getState() {
        return state;
    }

    public void setPrepare(Set<TransactionPrepare> prepare) {
        this.prepare = prepare;
    }

    public Set<TransactionPrepare> getPrepare() {
        return prepare;
    }

    public void setOffsets(Set<TransactionOffset> offsets) {
        this.offsets = offsets;
    }

    public Set<TransactionOffset> getOffsets() {
        return offsets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionMetadata that = (TransactionMetadata) o;
        return timeout == that.timeout &&
                producerId == that.producerId &&
                producerEpoch == that.producerEpoch &&
                Objects.equals(app, that.app);
    }

    @Override
    public int hashCode() {
        return Objects.hash(app, timeout, producerId, producerEpoch);
    }

    @Override
    public String toString() {
        return "TransactionMetadata{" +
                "app='" + app + '\'' +
                ", timeout=" + timeout +
                ", producerId=" + producerId +
                ", producerEpoch=" + producerEpoch +
                ", epoch=" + epoch +
                ", createTime=" + createTime +
                ", lastTime=" + lastTime +
                ", state=" + state +
                ", prepare=" + prepare +
                ", offsets=" + offsets +
                '}';
    }
}