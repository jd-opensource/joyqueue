package io.chubao.joyqueue.broker.monitor.service.support;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.broker.monitor.converter.BrokerMonitorConverter;
import io.chubao.joyqueue.broker.monitor.service.PartitionMonitorService;
import io.chubao.joyqueue.monitor.PartitionGroupMonitorInfo;
import io.chubao.joyqueue.monitor.PartitionMonitorInfo;
import io.chubao.joyqueue.broker.monitor.stat.AppStat;
import io.chubao.joyqueue.broker.monitor.stat.BrokerStat;
import io.chubao.joyqueue.broker.monitor.stat.PartitionGroupStat;
import io.chubao.joyqueue.broker.monitor.stat.PartitionStat;
import io.chubao.joyqueue.broker.monitor.stat.TopicStat;
import io.chubao.joyqueue.store.StoreManagementService;

import java.util.List;
import java.util.Map;

/**
 * PartitionMonitorService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/15
 */
public class DefaultPartitionMonitorService implements PartitionMonitorService {

    private BrokerStat brokerStat;
    private StoreManagementService storeManagementService;

    public DefaultPartitionMonitorService(BrokerStat brokerStat, StoreManagementService storeManagementService) {
        this.brokerStat = brokerStat;
        this.storeManagementService = storeManagementService;
    }

    @Override
    public PartitionMonitorInfo getPartitionInfoByTopic(String topic, short partition) {
        PartitionStat partitionStat = brokerStat.getOrCreateTopicStat(topic).getPartitionStat(partition);
        return convertPartitionMonitorInfo(partitionStat);
    }

    @Override
    public List<PartitionMonitorInfo> getPartitionInfosByTopic(String topic) {
        TopicStat topicStat = brokerStat.getOrCreateTopicStat(topic);
        List<PartitionMonitorInfo> result = Lists.newLinkedList();

        for (Map.Entry<Integer, PartitionGroupStat> entry : topicStat.getPartitionGroupStatMap().entrySet()) {
            for (Map.Entry<Short, PartitionStat> partitionStatEntry : entry.getValue().getPartitionStatMap().entrySet()) {
                PartitionMonitorInfo partitionMonitorInfo = convertPartitionMonitorInfo(partitionStatEntry.getValue());
                result.add(partitionMonitorInfo);
            }
        }

        return result;
    }

    @Override
    public PartitionMonitorInfo getPartitionInfoByTopicAndApp(String topic, String app, short partition) {
        PartitionStat partitionStat = brokerStat.getOrCreateTopicStat(topic).getOrCreateAppStat(app).getPartitionStat(partition);
        return convertPartitionMonitorInfo(partitionStat);
    }

    @Override
    public List<PartitionMonitorInfo> getPartitionInfosByTopicAndApp(String topic, String app) {
        AppStat appStat = brokerStat.getOrCreateTopicStat(topic).getOrCreateAppStat(app);
        List<PartitionMonitorInfo> result = Lists.newLinkedList();

        for (Map.Entry<Integer, PartitionGroupStat> entry : appStat.getPartitionGroupStatMap().entrySet()) {
            for (Map.Entry<Short, PartitionStat> partitionStatEntry : entry.getValue().getPartitionStatMap().entrySet()) {
                PartitionMonitorInfo partitionMonitorInfo = convertPartitionMonitorInfo(partitionStatEntry.getValue());
                result.add(partitionMonitorInfo);
            }
        }

        return result;
    }

    @Override
    public PartitionGroupMonitorInfo getPartitionGroupInfoByTopic(String topic, int partitionGroup) {
        PartitionGroupStat partitionGroupStat = brokerStat.getOrCreateTopicStat(topic).getOrCreatePartitionGroupStat(partitionGroup);
        return convertPartitionGroupMonitorInfo(partitionGroupStat);
    }

    @Override
    public List<PartitionGroupMonitorInfo> getPartitionGroupInfosByTopic(String topic) {
        TopicStat topicStat = brokerStat.getOrCreateTopicStat(topic);
        List<PartitionGroupMonitorInfo> result = Lists.newLinkedList();
        for (Map.Entry<Integer, PartitionGroupStat> entry : topicStat.getPartitionGroupStatMap().entrySet()) {
            PartitionGroupMonitorInfo partitionGroupMonitorInfo = convertPartitionGroupMonitorInfo(entry.getValue());
            result.add(partitionGroupMonitorInfo);

        }
        return result;
    }

    @Override
    public PartitionGroupMonitorInfo getPartitionGroupInfoByTopicAndApp(String topic, String app, int partitionGroup) {
        PartitionGroupStat partitionGroupStat = brokerStat.getOrCreateTopicStat(topic).getOrCreateAppStat(app).getOrCreatePartitionGroupStat(partitionGroup);
        return convertPartitionGroupMonitorInfo(partitionGroupStat);
    }

    @Override
    public List<PartitionGroupMonitorInfo> getPartitionGroupInfosByTopicAndApp(String topic, String app) {
        AppStat appStat = brokerStat.getOrCreateTopicStat(topic).getOrCreateAppStat(app);
        List<PartitionGroupMonitorInfo> result = Lists.newLinkedList();
        for (Map.Entry<Integer, PartitionGroupStat> entry : appStat.getPartitionGroupStatMap().entrySet()) {
            PartitionGroupMonitorInfo partitionGroupMonitorInfo = convertPartitionGroupMonitorInfo(entry.getValue());
            result.add(partitionGroupMonitorInfo);
        }
        return result;
    }

    protected PartitionGroupMonitorInfo convertPartitionGroupMonitorInfo(PartitionGroupStat partitionGroupStat) {
        StoreManagementService.PartitionGroupMetric partitionGroupMetric = storeManagementService.partitionGroupMetric(partitionGroupStat.getTopic(), partitionGroupStat.getPartitionGroup());
        PartitionGroupMonitorInfo partitionGroupMonitorInfo = new PartitionGroupMonitorInfo();
        partitionGroupMonitorInfo.setTopic(partitionGroupStat.getTopic());
        partitionGroupMonitorInfo.setApp(partitionGroupStat.getApp());
        partitionGroupMonitorInfo.setPartitionGroup(partitionGroupStat.getPartitionGroup());
        partitionGroupMonitorInfo.setLeftPosition(partitionGroupMetric.getLeftPosition());
        partitionGroupMonitorInfo.setRightPosition(partitionGroupMetric.getRightPosition());
        partitionGroupMonitorInfo.setIndexPosition(partitionGroupMetric.getIndexPosition());
        partitionGroupMonitorInfo.setFlushPosition(partitionGroupMetric.getFlushPosition());
        partitionGroupMonitorInfo.setReplicationPosition(partitionGroupMetric.getReplicationPosition());
        partitionGroupMonitorInfo.setEnQueue(BrokerMonitorConverter.convertEnQueueMonitorInfo(partitionGroupStat.getEnQueueStat()));
        partitionGroupMonitorInfo.setDeQueue(BrokerMonitorConverter.convertDeQueueMonitorInfo(partitionGroupStat.getDeQueueStat()));
        partitionGroupMonitorInfo.setReplication(BrokerMonitorConverter.convertReplicationMonitorInfo(partitionGroupStat.getReplicationStat()));
        return partitionGroupMonitorInfo;
    }

    protected PartitionMonitorInfo convertPartitionMonitorInfo(PartitionStat partitionStat) {
        PartitionMonitorInfo partitionMonitorInfo = new PartitionMonitorInfo();
        partitionMonitorInfo.setTopic(partitionStat.getTopic());
        partitionMonitorInfo.setApp(partitionStat.getApp());
        partitionMonitorInfo.setPartition(partitionStat.getPartition());
        partitionMonitorInfo.setEnQueue(BrokerMonitorConverter.convertEnQueueMonitorInfo(partitionStat.getEnQueueStat()));
        partitionMonitorInfo.setDeQueue(BrokerMonitorConverter.convertDeQueueMonitorInfo(partitionStat.getDeQueueStat()));
        return partitionMonitorInfo;
    }
}
