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

import java.util.concurrent.ConcurrentMap;

/**
 * ProducerStat
 *
 * author: gaohaoxiang
 * date: 2018/10/11
 */
public class ProducerStat {

    private String topic;
    private String app;
    private EnQueueStat enQueueStat = new EnQueueStat();
    private ConnectionStat connectionStat = new ConnectionStat();
    private ConcurrentMap<Integer /** partitionGroupId **/, PartitionGroupStat> partitionGroupStatMap = Maps.newConcurrentMap();

    public ProducerStat(String topic, String app) {
        this.topic = topic;
        this.app = app;
    }

    public PartitionGroupStat getOrCreatePartitionGroupStat(int partitionGroup) {
        PartitionGroupStat partitionGroupStat = partitionGroupStatMap.get(partitionGroup);
        if (partitionGroupStat == null) {
            partitionGroupStatMap.putIfAbsent(partitionGroup, new PartitionGroupStat(topic, app, partitionGroup));
            partitionGroupStat = partitionGroupStatMap.get(partitionGroup);
        }
        return partitionGroupStat;
    }

    public void clear() {
        enQueueStat = new EnQueueStat();
        connectionStat = new ConnectionStat();
        partitionGroupStatMap.clear();
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

    public ConnectionStat getConnectionStat() {
        return connectionStat;
    }

    public ConcurrentMap<Integer, PartitionGroupStat> getPartitionGroupStatMap() {
        return partitionGroupStatMap;
    }
}