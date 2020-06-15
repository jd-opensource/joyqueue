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
package org.joyqueue.store;

import org.joyqueue.domain.QosLevel;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
/**
 * @author liyue25
 * Date: 2018/8/13
 */
public class QosStore implements PartitionGroupStore {
    private static final Logger logger = LoggerFactory.getLogger(QosStore.class);
    private final PartitionGroupStoreManager store;
    private final QosLevel qosLevel;

    public QosStore(PartitionGroupStoreManager store, QosLevel qosLevel) {
        this.store = store;
        this.qosLevel = qosLevel;
    }

    @Override
    public String getTopic() {
        return store.getTopic();
    }

    @Override
    public int getPartitionGroup() {
        return store.getPartitionGroup();
    }

    @Override
    public Short[] listPartitions() {
        return store.listPartitions();
    }

    @Override
    public long getLeftIndex(short partition) {
        return store.getLeftIndex(partition);
    }

    @Override
    public long getRightIndex(short partition) {
        return store.getRightIndex(partition);
    }


//    public long getTotalPhysicalStorageSize() {
//        return store.messageStore().physicalSize() + store.meetPositioningStores().stream().mapToLong(PositioningStore::physicalSize).sum();
//    }

//
//    public long clean(long time, Map<Short, Long> partitionAckMap, boolean keepUnconsumed) throws IOException {
//        return store.clean(time, partitionAckMap, keepUnconsumed);
//    }

    /**
     * 根据消息存储时间获取索引。
     * 如果找到，返回最后一条 “存储时间 <= timestamp” 消息的索引。
     * 如果找不到，返回负值。
     *
     * @param partition
     * @param timestamp
     */
    @Override
    public long getIndex(short partition, long timestamp) {
        return store.getIndex(partition, timestamp);
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public CompletableFuture<WriteResult> asyncWrite(QosLevel qosLevel, WriteRequest... writeRequests) {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public void asyncWrite(EventListener<WriteResult> eventListener, QosLevel qosLevel, WriteRequest... writeRequests) {
        store.asyncWrite(qosLevel, eventListener, writeRequests);
    }

    @Override
    public boolean readable() {
        return true;
    }

    @Override
    public boolean writable() {
        return store.serviceStatus();
    }

    @Override
    public CompletableFuture<Boolean> whenClusterReady(long timeoutMs) {
        CompletableFuture<Boolean> clusterStartFuture=new CompletableFuture();
        clusterStartFuture.complete(true);
        return clusterStartFuture;
    }

    @Override
    public ReadResult read(short partition, long index, int count, long maxSize) throws IOException {

        return store.read(partition, index, count, maxSize);
    }
}
