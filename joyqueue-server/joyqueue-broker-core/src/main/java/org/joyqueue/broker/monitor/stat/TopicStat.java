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
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * TopicStat
 *
 * author: gaohaoxiang
 * date: 2018/10/11
 */
public class TopicStat implements Serializable {
    private static final long serialVersionUID = -2410477972367211215L;

    private String topic;
    private ConnectionStat connectionStat = new ConnectionStat();
    private EnQueueStat enQueueStat = new EnQueueStat();
    private DeQueueStat deQueueStat = new DeQueueStat();
    private ConcurrentMap<String /** app **/, AppStat> appStatMap = Maps.newConcurrentMap();
    // topic level partition group state
    private ConcurrentMap<Integer /** partitionGroupId **/, PartitionGroupStat> partitionGroupStatMap = Maps.newConcurrentMap();

    // 不能放这
    private long storeSize;

    public TopicStat(String topic) {
        this.topic = topic;
    }

    public PartitionStat getPartitionStat(short partition) {
        for (Map.Entry<Integer, PartitionGroupStat> entry : partitionGroupStatMap.entrySet()) {
            PartitionStat partitionStat = entry.getValue().getPartitionStatMap().get(partition);
            if (partitionStat != null) {
                return partitionStat;
            }
        }
        return new PartitionStat(topic, null, partition);
    }

    public PartitionGroupStat getOrCreatePartitionGroupStat(int partitionGroup) {
        PartitionGroupStat partitionGroupStat = partitionGroupStatMap.get(partitionGroup);
        if (partitionGroupStat == null) {
            partitionGroupStatMap.putIfAbsent(partitionGroup, new PartitionGroupStat(topic, null, partitionGroup));
            partitionGroupStat = partitionGroupStatMap.get(partitionGroup);
        }
        return partitionGroupStat;
    }

    public AppStat getOrCreateAppStat(String app) {
        AppStat appStat = appStatMap.get(app);
        if (appStat == null) {
            appStatMap.putIfAbsent(app, new AppStat(topic, app));
            appStat = appStatMap.get(app);
        }
        return appStat;
    }

    public void removePartitionGroup(int partitionGroup) {
        partitionGroupStatMap.remove(partitionGroup);
        for (Map.Entry<String, AppStat> entry : appStatMap.entrySet()) {
            entry.getValue().removePartitionGroup(partitionGroup);
        }
    }

    public void removePartition(short partition) {
        for (Map.Entry<Integer, PartitionGroupStat> entry : partitionGroupStatMap.entrySet()) {
            entry.getValue().getPartitionStatMap().remove(partition);
        }
        for (Map.Entry<String, AppStat> entry : appStatMap.entrySet()) {
            entry.getValue().removePartition(partition);
        }
    }

    public String getTopic() {
        return topic;
    }

    public ConcurrentMap<String, AppStat> getAppStats() {
        return appStatMap;
    }

    public EnQueueStat getEnQueueStat() {
        return enQueueStat;
    }

    public DeQueueStat getDeQueueStat() {
        return deQueueStat;
    }

    public ConnectionStat getConnectionStat() {
        return connectionStat;
    }

    public ConcurrentMap<Integer, PartitionGroupStat> getPartitionGroupStatMap() {
        return partitionGroupStatMap;
    }

    public void setStoreSize(long storeSize) {
        this.storeSize = storeSize;
    }

    public long getStoreSize() {
        return storeSize;
    }
}
