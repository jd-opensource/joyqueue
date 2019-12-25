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
 * ReplicationMonitorInfo
 *
 * author: gaohaoxiang
 * date: 2018/11/16
 */
public class ReplicationMonitorInfo extends BaseMonitorInfo {

    private String topic;
    private int partitionGroup;
    private EnQueueMonitorInfo replicaStat;
    private EnQueueMonitorInfo appendStat;
    private boolean started;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setPartitionGroup(int partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public EnQueueMonitorInfo getReplicaStat() {
        return replicaStat;
    }

    public void setReplicaStat(EnQueueMonitorInfo replicaStat) {
        this.replicaStat = replicaStat;
    }

    public EnQueueMonitorInfo getAppendStat() {
        return appendStat;
    }

    public void setAppendStat(EnQueueMonitorInfo appendStat) {
        this.appendStat = appendStat;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isStarted() {
        return started;
    }
}