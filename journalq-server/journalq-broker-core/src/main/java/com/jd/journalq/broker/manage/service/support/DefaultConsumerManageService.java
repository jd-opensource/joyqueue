package com.jd.journalq.broker.manage.service.support;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.manage.service.ConsumerManageService;
import com.jd.journalq.monitor.PartitionAckMonitorInfo;
import com.jd.journalq.network.session.Consumer;
import com.jd.journalq.store.PartitionGroupStore;
import com.jd.journalq.store.StoreManagementService;
import com.jd.journalq.store.StoreService;

import java.util.ArrayList;
import java.util.List;

/**
 * ConsumerManageService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/18
 */
public class DefaultConsumerManageService implements ConsumerManageService {

    private Consume consume;
    private StoreManagementService storeManagementService;
    private StoreService storeService;

    public DefaultConsumerManageService(Consume consume, StoreManagementService storeManagementService, StoreService storeService) {
        this.consume = consume;
        this.storeManagementService = storeManagementService;
        this.storeService = storeService;
    }

    @Override
    public boolean setAckIndex(String topic, String app, short partition, long index) {
        Consumer consumer = new Consumer();
        consumer.setTopic(topic);
        consumer.setApp(app);
        consume.setAckIndex(consumer, partition, index);
        return true;
    }

    @Override
    public boolean setMaxAckIndex(String topic, String app, short partition) {
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
                long index = consume.getAckIndex(consumer, partitionMetric.getPartition());
                result.add(new PartitionAckMonitorInfo(partitionMetric.getPartition(), index, partitionMetric.getLeftIndex(), partitionMetric.getRightIndex()));
            }
        }
        return result;
    }

    @Override
    public boolean setMaxAckIndexes(String topic, String app) {
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
    public boolean setAckIndexByTime(String topic, String app, short partition, long timestamp) {
        StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(topic);
        for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
            for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                if (partitionMetric.getPartition() == partition) {
                    return setPartitionAckIndexByTime(topic, app, partitionGroupMetric.getPartitionGroup(), partitionMetric.getPartition(), timestamp);
                }
            }
        }
        return false;
    }

    @Override
    public boolean setAckIndexesByTime(String topic, String app, long timestamp) {
        StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(topic);
        for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
            for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
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
    protected boolean setPartitionAckIndexByTime(String topic, String app, int partitionGroup, short partition, long timestamp) {
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