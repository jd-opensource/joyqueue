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
package org.joyqueue.broker.monitor.service.support;

import com.google.common.collect.Lists;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.monitor.converter.BrokerMonitorConverter;
import org.joyqueue.broker.monitor.service.ProducerMonitorService;
import org.joyqueue.broker.monitor.stat.AppStat;
import org.joyqueue.broker.monitor.stat.BrokerStat;
import org.joyqueue.broker.monitor.stat.PartitionGroupStat;
import org.joyqueue.broker.monitor.stat.PartitionStat;
import org.joyqueue.broker.monitor.stat.ProducerStat;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.model.Pager;
import org.joyqueue.monitor.ProducerMonitorInfo;
import org.joyqueue.monitor.ProducerPartitionGroupMonitorInfo;
import org.joyqueue.monitor.ProducerPartitionMonitorInfo;
import org.joyqueue.store.StoreManagementService;

import java.util.Collections;
import java.util.List;

/**
 * ProducerMonitorService
 *
 * author: gaohaoxiang
 * date: 2018/10/15
 */
public class DefaultProducerMonitorService implements ProducerMonitorService {

    private BrokerStat brokerStat;
    private StoreManagementService storeManagementService;
    private ClusterManager clusterManager;

    public DefaultProducerMonitorService(BrokerStat brokerStat, StoreManagementService storeManagementService, ClusterManager clusterManager) {
        this.brokerStat = brokerStat;
        this.storeManagementService = storeManagementService;
        this.clusterManager = clusterManager;
    }

    @Override
    public Pager<ProducerMonitorInfo> getProduceInfos(int page, int pageSize) {
        int total = 0;
        int startIndex = (page - 1) * pageSize;
        int endIndex = startIndex + pageSize;
        int index = 0;
        List<ProducerMonitorInfo> data = Lists.newArrayListWithCapacity(pageSize);

        for (TopicConfig topic : clusterManager.getTopics()) {
            List<org.joyqueue.domain.Producer> producers = clusterManager.getLocalProducersByTopic(topic.getName());
            for (org.joyqueue.domain.Producer producer : producers) {
                AppStat appStat = brokerStat.getOrCreateTopicStat(topic.getName().getFullName()).getOrCreateAppStat(producer.getApp());
                data.add(convertProducerMonitorInfo(appStat.getProducerStat()));
            }
        }

        Collections.sort(data, (o1, o2) -> {
            return Long.compare(o2.getEnQueue().getCount(), o1.getEnQueue().getCount());
        });

        return new Pager<>(page, pageSize, total, data);
    }

    @Override
    public ProducerMonitorInfo getProducerInfoByTopicAndApp(String topic, String app) {
        AppStat appStat = brokerStat.getOrCreateTopicStat(topic).getOrCreateAppStat(app);
        return convertProducerMonitorInfo(appStat.getProducerStat());
    }

    @Override
    public List<ProducerPartitionMonitorInfo> getProducerPartitionInfos(String topic, String app) {
        ProducerStat producerStat = brokerStat.getOrCreateTopicStat(topic).getOrCreateAppStat(app).getProducerStat();
        List<ProducerPartitionMonitorInfo> result = Lists.newLinkedList();
        StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(producerStat.getTopic());

        for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
            PartitionGroupStat partitionGroupStat = producerStat.getOrCreatePartitionGroupStat(partitionGroupMetric.getPartitionGroup());
            for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                if (!clusterManager.isLeader(topic, partitionMetric.getPartition())) {
                    continue;
                }
                PartitionStat partitionStat = partitionGroupStat.getOrCreatePartitionStat(partitionMetric.getPartition());
                ProducerPartitionMonitorInfo producerPartitionMonitorInfo = new ProducerPartitionMonitorInfo();
                producerPartitionMonitorInfo.setPartition(partitionMetric.getPartition());
                producerPartitionMonitorInfo.setTopic(topic);
                producerPartitionMonitorInfo.setApp(app);
                producerPartitionMonitorInfo.setEnQueue(BrokerMonitorConverter.convertEnQueueMonitorInfo(partitionStat.getEnQueueStat()));
                result.add(producerPartitionMonitorInfo);
            }
        }
        return result;
    }

    @Override
    public ProducerPartitionMonitorInfo getProducerPartitionInfoByTopicAndApp(String topic, String app, short partition) {
        ProducerStat producerStat = brokerStat.getOrCreateTopicStat(topic).getOrCreateAppStat(app).getProducerStat();
        return convertProducerPartitionMonitorInfo(producerStat, partition);
    }

    @Override
    public List<ProducerPartitionGroupMonitorInfo> getProducerPartitionGroupInfos(String topic, String app) {
        ProducerStat producerStat = brokerStat.getOrCreateTopicStat(topic).getOrCreateAppStat(app).getProducerStat();
        List<ProducerPartitionGroupMonitorInfo> result = Lists.newLinkedList();
        StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(producerStat.getTopic());

        for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
            if (!clusterManager.isLeader(topic, partitionGroupMetric.getPartitionGroup())) {
                continue;
            }
            result.add(convertProducerPartitionGroupMonitorInfo(producerStat, partitionGroupMetric.getPartitionGroup()));
        }
        return result;
    }

    @Override
    public ProducerPartitionGroupMonitorInfo getProducerPartitionGroupInfoByTopicAndApp(String topic, String app, int partitionGroupId) {
        ProducerStat producerStat = brokerStat.getOrCreateTopicStat(topic).getOrCreateAppStat(app).getProducerStat();
        return convertProducerPartitionGroupMonitorInfo(producerStat, partitionGroupId);
    }

    protected ProducerPartitionGroupMonitorInfo convertProducerPartitionGroupMonitorInfo(ProducerStat producerStat, int partitionGroupId) {
        ProducerPartitionGroupMonitorInfo producerPartitionGroupMonitorInfo = new ProducerPartitionGroupMonitorInfo();
        producerPartitionGroupMonitorInfo.setTopic(producerStat.getTopic());
        producerPartitionGroupMonitorInfo.setApp(producerStat.getApp());
        producerPartitionGroupMonitorInfo.setPartitionGroupId(partitionGroupId);
        producerPartitionGroupMonitorInfo.setEnQueue(BrokerMonitorConverter.convertEnQueueMonitorInfo(producerStat.getOrCreatePartitionGroupStat(partitionGroupId).getEnQueueStat()));
        return producerPartitionGroupMonitorInfo;
    }

    protected ProducerPartitionMonitorInfo convertProducerPartitionMonitorInfo(ProducerStat producerStat, short partition) {
        ProducerPartitionMonitorInfo producerPartitionMonitorInfo = new ProducerPartitionMonitorInfo();
        producerPartitionMonitorInfo.setTopic(producerStat.getTopic());
        producerPartitionMonitorInfo.setApp(producerStat.getApp());
        producerPartitionMonitorInfo.setPartition(partition);
        producerPartitionMonitorInfo.setEnQueue(BrokerMonitorConverter.convertEnQueueMonitorInfo(producerStat.getEnQueueStat()));
        return producerPartitionMonitorInfo;
    }

    protected ProducerMonitorInfo convertProducerMonitorInfo(ProducerStat producerStat) {
        ProducerMonitorInfo producerMonitorInfo = new ProducerMonitorInfo();
        producerMonitorInfo.setTopic(producerStat.getTopic());
        producerMonitorInfo.setApp(producerStat.getApp());
        producerMonitorInfo.setConnections(producerStat.getConnectionStat().getConnection());
        producerMonitorInfo.setEnQueue(BrokerMonitorConverter.convertEnQueueMonitorInfo(producerStat.getEnQueueStat()));
        return producerMonitorInfo;
    }
}
