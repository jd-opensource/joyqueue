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
package org.joyqueue.broker.manage.service.support;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.manage.service.ConsumerManageService;
import org.joyqueue.broker.monitor.ConsumerMonitor;
import org.joyqueue.broker.monitor.stat.PartitionStat;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.monitor.PartitionAckMonitorInfo;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.store.PartitionGroupStore;
import org.joyqueue.store.StoreManagementService;
import org.joyqueue.store.StoreService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ConsumerManageService
 *
 * author: gaohaoxiang
 * date: 2018/10/18
 */
public class DefaultConsumerManageService implements ConsumerManageService {

    private Consume consume;
    private StoreManagementService storeManagementService;
    private StoreService storeService;
    private ClusterManager clusterManager;
    private ConsumerMonitor consumerMonitor;

    public DefaultConsumerManageService(Consume consume, StoreManagementService storeManagementService, StoreService storeService,
                                        ClusterManager clusterManager,ConsumerMonitor consumerMonitor) {
        this.consume = consume;
        this.storeManagementService = storeManagementService;
        this.storeService = storeService;
        this.clusterManager = clusterManager;
        this.consumerMonitor = consumerMonitor;
    }

    @Override
    public boolean setAckIndex(String topic, String app, short partition, long index) throws JoyQueueException {
        Consumer consumer = new Consumer();
        consumer.setTopic(topic);
        consumer.setApp(app);
        consume.setAckIndex(consumer, partition, index);
        return true;
    }

    @Override
    public boolean setMaxAckIndex(String topic, String app, short partition) throws JoyQueueException {
        StoreManagementService.PartitionMetric partitionMetric = storeManagementService.partitionMetric(topic, partition);
        Consumer consumer = new Consumer();
        consumer.setTopic(topic);
        consumer.setApp(app);
        consume.setAckIndex(consumer, partition, partitionMetric.getRightIndex());
        return true;
    }

    @Override
    public long getAckIndex(String topic, String app, short partition) {
        Consumer consumer = new Consumer();
        consumer.setTopic(topic);
        consumer.setApp(app);
        return consume.getAckIndex(consumer, partition);
    }

    @Override
    public List<PartitionAckMonitorInfo> getAckIndexes(String topic, String app) {
        List<PartitionAckMonitorInfo> result = Lists.newArrayList();
        Consumer consumer = new Consumer();
        consumer.setTopic(topic);
        consumer.setApp(app);

        StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(topic);
        for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
            for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                if (!clusterManager.isLeader(topic, partitionMetric.getPartition())) {
                    continue;
                }
                long index = consume.getAckIndex(consumer, partitionMetric.getPartition());
                PartitionStat partitionStat = consumerMonitor.getConsumerStat(consumer.getTopic(), consumer.getApp())
                        .getOrCreatePartitionGroupStat(partitionGroupMetric.getPartitionGroup()).getOrCreatePartitionStat(partitionMetric.getPartition());
                long lastAckTime = partitionStat.getLastAckTime();
                long lastPullTime = partitionStat.getLastPullTime();
                result.add(new PartitionAckMonitorInfo(partitionMetric.getPartition(), index, lastPullTime, lastAckTime, partitionMetric.getLeftIndex(), partitionMetric.getRightIndex()));
            }
        }
        return result;
    }

    @Override
    public boolean setMaxAckIndexes(String topic, String app) throws JoyQueueException {
        StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(topic);
        Consumer consumer = new Consumer();
        consumer.setTopic(topic);
        consumer.setApp(app);
        for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
            for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                consume.setAckIndex(consumer, partitionMetric.getPartition(), partitionMetric.getRightIndex());
            }
        }
        return true;
    }

    @Override
    public boolean setAckIndexByTime(String topic, String app, short partition, long timestamp) throws JoyQueueException {
        StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(topic);
        for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
            for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                if (partitionMetric.getPartition() == partition) {
                    if (!clusterManager.isLeader(topic, partitionMetric.getPartition())) {
                        continue;
                    }
                    return setPartitionAckIndexByTime(topic, app, partitionGroupMetric.getPartitionGroup(), partitionMetric.getPartition(), timestamp);
                }
            }
        }
        return false;
    }

    @Override
    public boolean setAckIndexesByTime(String topic, String app, long timestamp) throws JoyQueueException {
        StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(topic);
        for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
            for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                if (!clusterManager.isLeader(topic, partitionMetric.getPartition())) {
                    continue;
                }
                setPartitionAckIndexByTime(topic, app, partitionGroupMetric.getPartitionGroup(), partitionMetric.getPartition(), timestamp);
            }
        }
        return true;
    }

    @Override
    public long getAckIndexByTime(String topic, String app, short partition, long timestamp) {
        StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(topic);
        for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
            for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                if (partitionMetric.getPartition() == partition) {
                    PartitionGroupStore store = storeService.getStore(topic, partitionGroupMetric.getPartitionGroup());
                    return store.getIndex(partition, timestamp);
                }
            }
        }
        return -1;
    }
    @Override
    public List<PartitionAckMonitorInfo> getTopicAckIndexByTime(String topic, String app, long timestamp) {
        List<PartitionAckMonitorInfo> partitionAckMonitorInfos = new ArrayList<>();
        StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(topic);
        for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
            PartitionGroupStore store = storeService.getStore(topic, partitionGroupMetric.getPartitionGroup());
            for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                PartitionAckMonitorInfo partitionAckMonitorInfo = new PartitionAckMonitorInfo();
                long index = store.getIndex(partitionMetric.getPartition(), timestamp);
                partitionAckMonitorInfo.setIndex(index);
                partitionAckMonitorInfo.setPartition(partitionMetric.getPartition());
                partitionAckMonitorInfos.add(partitionAckMonitorInfo);
            }
        }
        return partitionAckMonitorInfos;
    }

    @Override
    public String initConsumerAckIndexes(boolean right) throws JoyQueueException {
        Map<String, List<String>> result = Maps.newHashMap();
        for (TopicConfig topicConfig : clusterManager.getTopics()) {
            List<String> apps = Lists.newLinkedList();
            result.put(topicConfig.getName().getFullName(), apps);
            List<org.joyqueue.domain.Consumer> consumers = clusterManager.getLocalConsumersByTopic(topicConfig.getName());

            if (CollectionUtils.isEmpty(consumers)) {
                continue;
            }

            for (PartitionGroup partitionGroup : clusterManager.getLocalPartitionGroups(topicConfig)) {
                if (!clusterManager.isLeader(partitionGroup.getTopic(), partitionGroup.getGroup())) {
                    continue;
                }
                PartitionGroupStore store = storeService.getStore(partitionGroup.getTopic().getFullName(), partitionGroup.getGroup());
                if (store == null) {
                    continue;
                }
                for (Short partition : partitionGroup.getPartitions()) {
                    for (org.joyqueue.domain.Consumer consumer : consumers) {
                        apps.add(consumer.getApp());
                        consume.setAckIndex(new Consumer(consumer.getTopic().getFullName(), consumer.getApp()), partition,
                                (right ? store.getRightIndex(partition) : store.getLeftIndex(partition)));
                    }
                }
            }
        }
        return JSON.toJSONString(result);
    }

    protected boolean setPartitionAckIndexByTime(String topic, String app, int partitionGroup, short partition, long timestamp) throws JoyQueueException {
        PartitionGroupStore store = storeService.getStore(topic, partitionGroup);
        long index = store.getIndex(partition, timestamp);
        if (index < 0) {
            return false;
        }
        Consumer consumer = new Consumer();
        consumer.setTopic(topic);
        consumer.setApp(app);
        consume.setAckIndex(consumer, partition, index);
        return true;
    }
}