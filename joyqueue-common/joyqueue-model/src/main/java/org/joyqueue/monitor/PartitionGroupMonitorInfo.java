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
package org.joyqueue.monitor;

/**
 * 分区组信息
 * @author lining11
 * Date: 2018/9/13
 */
public class PartitionGroupMonitorInfo extends BaseMonitorInfo {

    private String topic;
    private String app;
    private int partitionGroup;
    private long leftPosition;
    private long rightPosition;
    private long indexPosition;
    private long flushPosition;
    private long replicationPosition;

    private EnQueueMonitorInfo enQueue;
    private DeQueueMonitorInfo deQueue;
    private ReplicationMonitorInfo replication;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public void setPartitionGroup(int partitionGroup) {
        this.partitionGroup = partitionGroup;
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

    public EnQueueMonitorInfo getEnQueue() {
        return enQueue;
    }

    public void setEnQueue(EnQueueMonitorInfo enQueue) {
        this.enQueue = enQueue;
    }

    public DeQueueMonitorInfo getDeQueue() {
        return deQueue;
    }

    public void setDeQueue(DeQueueMonitorInfo deQueue) {
        this.deQueue = deQueue;
    }

    public void setReplication(ReplicationMonitorInfo replication) {
        this.replication = replication;
    }

    public ReplicationMonitorInfo getReplication() {
        return replication;
    }
}