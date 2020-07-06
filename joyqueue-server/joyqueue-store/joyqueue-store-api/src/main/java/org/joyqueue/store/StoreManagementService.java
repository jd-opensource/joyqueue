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


import java.io.File;

/**
 * 存储监控和管理服务
 * @author liyue25
 * Date: 2018/10/18
 */
public interface StoreManagementService {

    /**
     * 获取Store度量信息
     */
    TopicMetric [] storeMetrics();

    /**
     * Topic度量信息
     */
    TopicMetric topicMetric(String topic);

    /**
     * PartitionGroup度量信息
     */
    PartitionGroupMetric partitionGroupMetric(String topic, int partitionGroup);

    /**
     * Topic度量信息
     */
    PartitionMetric partitionMetric(String topic, short partition);

    /**
     * 列出store中给定path的所有文件
     * @param path 相对store根目录的相对路径
     */
    File [] listFiles(String path);
    File [] listFiles(File directory);

    /**
     * 返回totalSpace
     * @return
     */
    long totalSpace();

    /**
     * 返回totalSpace
     * @return
     */
    long freeSpace();

    /**
     * 读取消息
     */
    byte [][] readMessages(String topic, int partitionGroup, long position, int count);
    byte [][] readMessages(String topic, short partition, long index, int count);
    byte [][] readMessages(File file, long position, int count, boolean includeFileHeader);
    /**
     * 读取索引
     */
    Long [] readIndices(String topic, short partition, long index, int count);
    Long [] readIndices(File file, long position, int count, boolean includeFileHeader);

    /**
     * 裸接口
     */
    byte [] readFile(File file, long position, int length);
    byte [] readPartitionGroupStore(String topic, int partitionGroup, long position, int length);



    class TopicMetric {
        private String topic;
        private PartitionGroupMetric [] partitionGroupMetrics;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public PartitionGroupMetric[] getPartitionGroupMetrics() {
            return partitionGroupMetrics;
        }

        public void setPartitionGroupMetrics(PartitionGroupMetric[] partitionGroupMetrics) {
            this.partitionGroupMetrics = partitionGroupMetrics;
        }

        public boolean isUsabled() {
            return !topic.startsWith(".");
        }
    }

    class PartitionGroupMetric {
        private int partitionGroup;
        private PartitionMetric [] partitionMetrics;
        // 最小位置、最大位置、已索引位置、刷盘位置、复制位置
        private long leftPosition, rightPosition, indexPosition, flushPosition, replicationPosition;

        public int getPartitionGroup() {
            return partitionGroup;
        }

        public void setPartitionGroup(int partitionGroup) {
            this.partitionGroup = partitionGroup;
        }

        public PartitionMetric[] getPartitionMetrics() {
            return partitionMetrics;
        }

        public void setPartitionMetrics(PartitionMetric[] partitionMetrics) {
            this.partitionMetrics = partitionMetrics;
        }

        public long getLeftPosition() {
            return leftPosition;
        }

        public void setLeftPosition(long leftPosition) {
            this.leftPosition = leftPosition;
        }

        public long getRightPosition() {
            return rightPosition;
        }

        public void setRightPosition(long rightPosition) {
            this.rightPosition = rightPosition;
        }

        public long getIndexPosition() {
            return indexPosition;
        }

        public void setIndexPosition(long indexPosition) {
            this.indexPosition = indexPosition;
        }

        public long getFlushPosition() {
            return flushPosition;
        }

        public void setFlushPosition(long flushPosition) {
            this.flushPosition = flushPosition;
        }

        public long getReplicationPosition() {
            return replicationPosition;
        }

        public void setReplicationPosition(long replicationPosition) {
            this.replicationPosition = replicationPosition;
        }
    }

    class PartitionMetric {
        private short partition;
        private long leftIndex, rightIndex; // 最小最大索引

        public short getPartition() {
            return partition;
        }

        public void setPartition(short partition) {
            this.partition = partition;
        }

        public long getLeftIndex() {
            return leftIndex;
        }

        public void setLeftIndex(long leftIndex) {
            this.leftIndex = leftIndex;
        }

        public long getRightIndex() {
            return rightIndex;
        }

        public void setRightIndex(long rightIndex) {
            this.rightIndex = rightIndex;
        }
    }

    class CacheMetric {
        long usedSize; // 当前用量
        long maxSize; // 最大容量
        private long accessCount;  // 请求次数
        private long hitCount; // 命中次数

        public long getUsedSize() {
            return usedSize;
        }

        public void setUsedSize(long usedSize) {
            this.usedSize = usedSize;
        }

        public long getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(long maxSize) {
            this.maxSize = maxSize;
        }

        public long getAccessCount() {
            return accessCount;
        }

        public void setAccessCount(long accessCount) {
            this.accessCount = accessCount;
        }

        public long getHitCount() {
            return hitCount;
        }

        public void setHitCount(long hitCount) {
            this.hitCount = hitCount;
        }
    }
}
