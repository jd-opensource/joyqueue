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
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.monitor.converter.BrokerMonitorConverter;
import org.joyqueue.broker.monitor.exception.MonitorException;
import org.joyqueue.broker.monitor.service.ConsumerMonitorService;
import org.joyqueue.broker.monitor.stat.AppStat;
import org.joyqueue.broker.monitor.stat.BrokerStat;
import org.joyqueue.broker.monitor.stat.ConsumerStat;
import org.joyqueue.broker.monitor.stat.PartitionGroupStat;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.model.Pager;
import org.joyqueue.monitor.ConsumerMonitorInfo;
import org.joyqueue.monitor.ConsumerPartitionGroupMonitorInfo;
import org.joyqueue.monitor.ConsumerPartitionMonitorInfo;
import org.joyqueue.monitor.PendingMonitorInfo;
import org.joyqueue.monitor.RetryMonitorInfo;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.server.retry.api.MessageRetry;
import org.joyqueue.store.StoreManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * ConsumerMonitorService
 *
 * author: gaohaoxiang
 * date: 2018/10/15
 */
public class DefaultConsumerMonitorService implements ConsumerMonitorService {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultConsumerMonitorService.class);

    private BrokerStat brokerStat;
    private Consume consume;
    private StoreManagementService storeManagementService;
    private MessageRetry retryManager;
    private ClusterManager clusterManager;

    public DefaultConsumerMonitorService(BrokerStat brokerStat, Consume consume, StoreManagementService storeManagementService, MessageRetry retryManager, ClusterManager clusterManager) {
        this.brokerStat = brokerStat;
        this.consume = consume;
        this.storeManagementService = storeManagementService;
        this.retryManager = retryManager;
        this.clusterManager = clusterManager;
    }

    @Override
    public Pager<ConsumerMonitorInfo> getConsumerInfos(int page, int pageSize) {
        int total = 0;
        int startIndex = (page - 1) * pageSize;
        int endIndex = startIndex + pageSize;
        int index = 0;
        List<ConsumerMonitorInfo> data = Lists.newArrayListWithCapacity(pageSize);

        for (TopicConfig topic : clusterManager.getTopics()) {
            List<org.joyqueue.domain.Consumer> consumers = clusterManager.getLocalConsumersByTopic(topic.getName());
            for (org.joyqueue.domain.Consumer consumer : consumers) {
                AppStat appStat = brokerStat.getOrCreateTopicStat(topic.getName().getFullName()).getOrCreateAppStat(consumer.getApp());
                data.add(convertConsumerMonitorInfo(appStat.getConsumerStat()));
            }
        }

        Collections.sort(data, (o1, o2) -> {
            return Long.compare(o2.getPending().getCount(), o1.getPending().getCount());
        });

        return new Pager<>(page, pageSize, total, data);
    }

    @Override
    public ConsumerMonitorInfo getConsumerInfoByTopicAndApp(String topic, String app) {
        ConsumerStat consumerStat = brokerStat.getOrCreateTopicStat(topic).getOrCreateAppStat(app).getConsumerStat();
        return convertConsumerMonitorInfo(consumerStat);
    }

    @Override
    public List<ConsumerPartitionMonitorInfo> getConsumerPartitionInfos(String topic, String app) {
        ConsumerStat consumerStat = brokerStat.getOrCreateTopicStat(topic).getOrCreateAppStat(app).getConsumerStat();
        List<ConsumerPartitionMonitorInfo> result = Lists.newLinkedList();

        StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(consumerStat.getTopic());
        for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
            for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                if (!clusterManager.isLeader(topic, partitionMetric.getPartition())) {
                    continue;
                }
                ConsumerPartitionMonitorInfo consumerPartitionMonitorInfo = convertConsumerPartitionMonitorInfo(consumerStat, partitionMetric.getPartition());
                result.add(consumerPartitionMonitorInfo);
            }
        }

        return result;
    }

    @Override
    public ConsumerPartitionMonitorInfo getConsumerPartitionInfoByTopicAndApp(String topic, String app, short partition) {
        StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(topic);
        ConsumerStat consumerStat = brokerStat.getOrCreateTopicStat(topic).getOrCreateAppStat(app).getConsumerStat();
        for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
            for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                if (!clusterManager.isLeader(topic, partitionMetric.getPartition())) {
                    continue;
                }
                if (partitionMetric.getPartition() == partition) {
                    return convertConsumerPartitionMonitorInfo(consumerStat, partition);
                }
            }
        }
        return null;
    }

    @Override
    public List<ConsumerPartitionGroupMonitorInfo> getConsumerPartitionGroupInfos(String topic, String app) {
        ConsumerStat consumerStat = brokerStat.getOrCreateTopicStat(topic).getOrCreateAppStat(app).getConsumerStat();
        List<ConsumerPartitionGroupMonitorInfo> result = Lists.newLinkedList();

        StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(consumerStat.getTopic());
        for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
            if (!clusterManager.isLeader(topic, partitionGroupMetric.getPartitionGroup())) {
                continue;
            }
            ConsumerPartitionGroupMonitorInfo consumerPartitionGroupMonitorInfo = convertConsumerPartitionGroupMonitorInfo(consumerStat, partitionGroupMetric.getPartitionGroup());
            result.add(consumerPartitionGroupMonitorInfo);
        }
        return result;
    }

    @Override
    public ConsumerPartitionGroupMonitorInfo getConsumerPartitionGroupInfoByTopicAndApp(String topic, String app, int partitionGroupId) {
        ConsumerStat consumerStat = brokerStat.getOrCreateTopicStat(topic).getOrCreateAppStat(app).getConsumerStat();
        return convertConsumerPartitionGroupMonitorInfo(consumerStat, partitionGroupId);
    }

    protected ConsumerPartitionGroupMonitorInfo convertConsumerPartitionGroupMonitorInfo(ConsumerStat consumerStat, int partitionGroupId) {
        PartitionGroupStat partitionGroupStat = consumerStat.getOrCreatePartitionGroupStat(partitionGroupId);
        PendingMonitorInfo pendingMonitorInfo = new PendingMonitorInfo();
        Consumer consumer = new Consumer(consumerStat.getTopic(), consumerStat.getApp());
        try {
            StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(consumerStat.getTopic());
            for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
                if (partitionGroupMetric.getPartitionGroup() == partitionGroupId) {
                    for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                        long ackIndex = consume.getAckIndex(consumer, partitionMetric.getPartition());
                        if (ackIndex < 0) {
                            ackIndex = 0;
                        }
                        if (ackIndex >= partitionMetric.getRightIndex()) {
                            continue;
                        }
                        pendingMonitorInfo.setCount(pendingMonitorInfo.getCount() + (partitionMetric.getRightIndex() - ackIndex));
                    }
                }
            }
        } catch (Exception e) {
            throw new MonitorException(e);
        }

        ConsumerPartitionGroupMonitorInfo consumerPartitionGroupMonitorInfo = new ConsumerPartitionGroupMonitorInfo();
        consumerPartitionGroupMonitorInfo.setTopic(consumerStat.getTopic());
        consumerPartitionGroupMonitorInfo.setApp(consumerStat.getApp());
        consumerPartitionGroupMonitorInfo.setPartitionGroupId(partitionGroupId);

        consumerPartitionGroupMonitorInfo.setDeQueue(BrokerMonitorConverter.convertDeQueueMonitorInfo(partitionGroupStat.getDeQueueStat()));
        consumerPartitionGroupMonitorInfo.setPending(pendingMonitorInfo);

        return consumerPartitionGroupMonitorInfo;
    }

    protected ConsumerPartitionMonitorInfo convertConsumerPartitionMonitorInfo(ConsumerStat consumerStat, short partition) {
        PendingMonitorInfo pendingMonitorInfo = new PendingMonitorInfo();
        int partitionGroup = 0;

        try {
            Consumer consumer = new Consumer(consumerStat.getTopic(), consumerStat.getApp());
            StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(consumerStat.getTopic());
            for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
                for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                    if (partitionMetric.getPartition() == partition) {
                        long ackIndex = consume.getAckIndex(consumer, partitionMetric.getPartition());
                        if (ackIndex < 0) {
                            ackIndex = 0;
                        }
                        partitionGroup = partitionGroupMetric.getPartitionGroup();
                        if (ackIndex >= partitionMetric.getRightIndex()) {
                            continue;
                        }
                        pendingMonitorInfo.setCount(partitionMetric.getRightIndex() - ackIndex);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new MonitorException(e);
        }

        ConsumerPartitionMonitorInfo consumerPartitionMonitorInfo = new ConsumerPartitionMonitorInfo();
        consumerPartitionMonitorInfo.setTopic(consumerStat.getTopic());
        consumerPartitionMonitorInfo.setApp(consumerStat.getApp());
        consumerPartitionMonitorInfo.setPartition(partition);

        consumerPartitionMonitorInfo.setDeQueue(BrokerMonitorConverter.convertDeQueueMonitorInfo(
                consumerStat.getOrCreatePartitionGroupStat(partitionGroup).getOrCreatePartitionStat(partition).getDeQueueStat()));
        consumerPartitionMonitorInfo.setPending(pendingMonitorInfo);

        return consumerPartitionMonitorInfo;
    }

    protected ConsumerMonitorInfo convertConsumerMonitorInfo(ConsumerStat consumerStat) {
        PendingMonitorInfo pendingMonitorInfo = new PendingMonitorInfo();
        try {
            Consumer consumer = new Consumer(consumerStat.getTopic(), consumerStat.getApp());
            StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(consumerStat.getTopic());
            for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
                for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                    if (!clusterManager.isLeader(consumer.getTopic(), partitionMetric.getPartition())) {
                        continue;
                    }
                    long ackIndex = consume.getAckIndex(consumer, partitionMetric.getPartition());
                    if (ackIndex < 0) {
                        ackIndex = 0;
                    }
                    if (ackIndex >= partitionMetric.getRightIndex()) {
                        continue;
                    }
                    pendingMonitorInfo.setCount(pendingMonitorInfo.getCount() + (partitionMetric.getRightIndex() - ackIndex));
                }
            }
        } catch (Exception e) {
            throw new MonitorException(e);
        }

        RetryMonitorInfo retryMonitorInfo = new RetryMonitorInfo();
        try {
            retryMonitorInfo.setCount(retryManager.countRetry(consumerStat.getTopic(), consumerStat.getApp()));
        } catch (JoyQueueException e) {
            logger.error("getRetry exception, topic: {}, app: {}", consumerStat.getTopic(), consumerStat.getApp(), e);
        }
        retryMonitorInfo.setCurrent(consumerStat.getRetryStat().getTotal().getOneMinuteRate());
        retryMonitorInfo.setSuccess(consumerStat.getRetryStat().getSuccess().getOneMinuteRate());
        retryMonitorInfo.setFailure(consumerStat.getRetryStat().getFailure().getOneMinuteRate());

        ConsumerMonitorInfo consumerMonitorInfo = new ConsumerMonitorInfo();
        consumerMonitorInfo.setTopic(consumerStat.getTopic());
        consumerMonitorInfo.setApp(consumerStat.getApp());
        consumerMonitorInfo.setConnections(consumerStat.getConnectionStat().getConnection());

        consumerMonitorInfo.setDeQueue(BrokerMonitorConverter.convertDeQueueMonitorInfo(consumerStat.getDeQueueStat()));
        consumerMonitorInfo.setRetry(retryMonitorInfo);
        consumerMonitorInfo.setPending(pendingMonitorInfo);

        return consumerMonitorInfo;
    }

}