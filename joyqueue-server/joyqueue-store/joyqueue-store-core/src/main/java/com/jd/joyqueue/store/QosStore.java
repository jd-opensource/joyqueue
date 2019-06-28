/**
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
package com.jd.joyqueue.store;

import com.jd.joyqueue.domain.QosLevel;
import com.jd.joyqueue.store.file.PositioningStore;
import com.jd.joyqueue.store.index.IndexItem;
import com.jd.joyqueue.toolkit.concurrent.EventFuture;
import com.jd.joyqueue.toolkit.concurrent.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Future;

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

    @Override
    public long getTotalPhysicalStorageSize() {
        return store.messageStore().physicalSize() + store.meetPositioningStores().stream().mapToLong(PositioningStore::physicalSize).sum();
    }

    @Override
    public long deleteMinStoreMessages(long targetDeleteTimeline, Map<Short, Long> partitionAckMap, boolean doNotDeleteConsumed) throws IOException {
        long deletedSize = 0L;

        // 计算最小索引的消息位置
        long minMessagePosition = -1L;
        for (Map.Entry<Short, Long> partition : partitionAckMap.entrySet()) {
            Short p = partition.getKey();
            long minPartitionIndex = partition.getValue();
            PositioningStore<IndexItem> indexStore = store.indexStore(p);
            if (indexStore != null) {
                if (minPartitionIndex != Long.MAX_VALUE && doNotDeleteConsumed) {
                    minPartitionIndex *= IndexItem.STORAGE_SIZE;
                } else {
                    minPartitionIndex = Long.MAX_VALUE;
                }
                if (targetDeleteTimeline <= 0) {
                    // 依次删除每个分区p索引中最左侧的文件 满足当前分区p的最小消费位置之前的文件块
                    if (indexStore.fileCount() > 1 && indexStore.meetMinStoreFile(minPartitionIndex) > 1) {
                        deletedSize += indexStore.physicalDeleteLeftFile();
                        logger.info("Delete PositioningStore physical index file by size, partition: <{}>, offset position: <{}>", p, minPartitionIndex);
                    }
                } else {
                    // 依次删除每个分区p索引中最左侧的文件 满足当前分区p的最小消费位置之前的以及最长时间戳的文件块
                    if (indexStore.fileCount() > 1 && indexStore.meetMinStoreFile(minPartitionIndex) > 1 && indexStore.isEarly(targetDeleteTimeline, minPartitionIndex)) {
                        deletedSize += indexStore.physicalDeleteLeftFile();
                        logger.info("Delete PositioningStore physical index file by time, partition: <{}>, offset position: <{}>", p, minPartitionIndex);
                    }
                }

                try {
                    long storeMinMessagePosition = indexStore.read(indexStore.left()).getOffset();
                    if (minMessagePosition < 0 || minMessagePosition > storeMinMessagePosition) {
                        minMessagePosition = storeMinMessagePosition;
                    }
                } catch (PositionOverflowException ignored) {
                }
            }
        }

        if(minMessagePosition >= 0) {
            deletedSize += store.messageStore().physicalDeleteTo(minMessagePosition);
            logger.info("Delete PositioningStore physical message file, offset position: <{}>", minMessagePosition);
        }

        return deletedSize;
    }

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
    public Future<WriteResult> asyncWrite(WriteRequest... writeRequests) {
        EventFuture<WriteResult> future = new EventFuture<>();
        asyncWrite(future, writeRequests);
        return future;
    }

    @Override
    public void asyncWrite(EventListener<WriteResult> eventListener, WriteRequest... writeRequests) {
        store.asyncWrite(this.qosLevel, eventListener, writeRequests);
    }

    @Override
    public ReadResult read(short partition, long index, int count, long maxSize) throws IOException {

        return store.read(partition, index, count, maxSize);
    }
}
