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

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

/**
 * PartitionGroupStat
 *
 * author: gaohaoxiang
 * date: 2018/10/11
 */
public class PartitionGroupStat implements Serializable {

    private String topic;
    private String app;
    private int partitionGroup;

    private EnQueueStat enQueueStat = new EnQueueStat();
    private DeQueueStat deQueueStat = new DeQueueStat();
    private ReplicationStat replicationStat = new ReplicationStat();
    private ElectionEventStat electionEventStat =new ElectionEventStat();
    // realtime state of Replica group, fill it before use

    private ConcurrentMap<Short /** partition **/, PartitionStat> partitionStatMap = Maps.newConcurrentMap();

    public PartitionGroupStat(String topic, String app, int partitionGroup) {
        this.topic = topic;
        this.app = app;
        this.partitionGroup = partitionGroup;
    }

    public PartitionStat getOrCreatePartitionStat(short partition) {
        PartitionStat partitionStat = partitionStatMap.get(partition);
        if (partitionStat == null) {
            partitionStatMap.putIfAbsent(partition, new PartitionStat(topic, app, partition));
            partitionStat = partitionStatMap.get(partition);
        }
        return partitionStat;
    }

    public String getTopic() {
        return topic;
    }

    public String getApp() {
        return app;
    }

    public EnQueueStat getEnQueueStat() {
        return enQueueStat;
    }

    public DeQueueStat getDeQueueStat() {
        return deQueueStat;
    }

    public ElectionEventStat getElectionEventStat() {
        return electionEventStat;
    }
    public int getPartitionGroup() {
        return partitionGroup;
    }

    public ConcurrentMap<Short, PartitionStat> getPartitionStatMap() {
        return partitionStatMap;
    }

    public ReplicationStat getReplicationStat() {
        return replicationStat;
    }
}
