/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.broker.monitor.stat;

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
    private ConcurrentMap<Integer /** partitionGroupId **/, PartitionGroupStat> partitionGroupStatMap = Maps.newConcurrentMap();

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
}
