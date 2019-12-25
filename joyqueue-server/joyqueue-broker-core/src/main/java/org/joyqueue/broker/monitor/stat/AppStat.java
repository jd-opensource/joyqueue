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
 * AppStat
 *
 * author: gaohaoxiang
 * date: 2018/11/16
 */
public class AppStat implements Serializable {

    private static final long serialVersionUID = -1925788827902461246L;

    private String topic;
    private String app;
    private ConnectionStat connectionStat = new ConnectionStat();
    // app level partition group state
    private ConcurrentMap<Integer /** partitionGroupId **/, PartitionGroupStat> partitionGroupStatMap = Maps.newConcurrentMap();

    private ConsumerStat consumerStat;
    private ProducerStat producerStat;

    public AppStat(String topic, String app) {
        this.topic = topic;
        this.app = app;
        this.consumerStat = new ConsumerStat(topic, app);
        this.producerStat = new ProducerStat(topic, app);
    }

    public PartitionStat getPartitionStat(short partition) {
        for (Map.Entry<Integer, PartitionGroupStat> entry : partitionGroupStatMap.entrySet()) {
            PartitionStat partitionStat = entry.getValue().getPartitionStatMap().get(partition);
            if (partitionStat != null) {
                return partitionStat;
            }
        }
        return new PartitionStat(topic, app, partition);
    }

    public PartitionGroupStat getOrCreatePartitionGroupStat(int partitionGroup) {
        PartitionGroupStat partitionGroupStat = partitionGroupStatMap.get(partitionGroup);
        if (partitionGroupStat == null) {
            partitionGroupStatMap.putIfAbsent(partitionGroup, new PartitionGroupStat(topic, app, partitionGroup));
            partitionGroupStat = partitionGroupStatMap.get(partitionGroup);
        }
        return partitionGroupStat;
    }

    public void removePartitionGroup(int partitionGroup) {
        partitionGroupStatMap.remove(partitionGroup);
    }

    public void removePartition(short partition) {
        for (Map.Entry<Integer, PartitionGroupStat> entry : partitionGroupStatMap.entrySet()) {
            entry.getValue().getPartitionStatMap().remove(partition);
        }
    }

    public String getTopic() {
        return topic;
    }

    public String getApp() {
        return app;
    }

    public ConnectionStat getConnectionStat() {
        return connectionStat;
    }

    public ConsumerStat getConsumerStat() {
        return consumerStat;
    }

    public ProducerStat getProducerStat() {
        return producerStat;
    }

    public ConcurrentMap<Integer, PartitionGroupStat> getPartitionGroupStatMap() {
        return partitionGroupStatMap;
    }
}
