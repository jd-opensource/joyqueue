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
package org.joyqueue.broker.monitor.stat;

import java.io.Serializable;

/**
 * ReplicationStat
 *
 * author: gaohaoxiang
 * date: 2018/11/16
 */
public class ReplicationStat implements Serializable {

    private String topic;
    private int partitionGroup;

    // replication out on leader
    private EnQueueStat replicaStat = new EnQueueStat();
    // replication in on follower
    private EnQueueStat appendStat = new EnQueueStat();
    // partition group replica state
    private ReplicaNodeStat stat=new ReplicaNodeStat();
    // partition group replica max log position
    private long  maxLogPosition;

    public ReplicationStat() {
    }

    public ReplicationStat(String topic, int partitionGroup) {
        this.topic = topic;
        this.partitionGroup = partitionGroup;
    }

    public String getTopic() {
        return topic;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public EnQueueStat getReplicaStat() {
        return replicaStat;
    }


    public long getMaxLogPosition() {
        return maxLogPosition;
    }

    public void setMaxLogPosition(long maxLogPosition) {
        this.maxLogPosition = maxLogPosition;
    }

    public EnQueueStat getAppendStat() {
        return appendStat;
    }

    public ReplicaNodeStat getStat() {
        return stat;
    }

}