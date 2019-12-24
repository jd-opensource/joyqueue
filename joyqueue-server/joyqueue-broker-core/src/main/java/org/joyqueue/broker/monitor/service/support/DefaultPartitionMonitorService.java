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
import org.joyqueue.broker.election.ElectionService;
import org.joyqueue.broker.monitor.converter.BrokerMonitorConverter;
import org.joyqueue.broker.monitor.service.PartitionMonitorService;
import org.joyqueue.broker.monitor.stat.AppStat;
import org.joyqueue.broker.monitor.stat.BrokerStat;
import org.joyqueue.broker.monitor.stat.ElectionEventStat;
import org.joyqueue.broker.monitor.stat.PartitionGroupStat;
import org.joyqueue.broker.monitor.stat.PartitionStat;
import org.joyqueue.broker.monitor.stat.ReplicaNodeStat;
import org.joyqueue.broker.monitor.stat.TopicStat;
import org.joyqueue.monitor.PartitionGroupMonitorInfo;
import org.joyqueue.monitor.PartitionMonitorInfo;
import org.joyqueue.store.StoreManagementService;

import java.util.List;
import java.util.Map;

/**
 * PartitionMonitorService
 *
 * author: gaohaoxiang
 * date: 2018/10/15
 */
public class DefaultPartitionMonitorService implements PartitionMonitorService {

    private BrokerStat brokerStat;
    private StoreManagementService storeManagementService;
    private ElectionService electionManager;
    public DefaultPartitionMonitorService(BrokerStat brokerStat, StoreManagementService storeManagementService, ElectionService electionManager) {
        this.brokerStat = brokerStat;
        this.storeManagementService = storeManagementService;
        this.electionManager=electionManager;
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
    public ElectionEventStat getReplicaRecentElectionEvent(String topic, int partitionGroup) {
        TopicStat topicStat= brokerStat.getTopicStats().get(topic);
        if(topicStat!=null) {
            PartitionGroupStat partitionGroupStat=   topicStat.getPartitionGroupStatMap().get(partitionGroup);
            if(partitionGroupStat!=null){
                return partitionGroupStat.getElectionEventStat();
            }
        }
        return null;
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

    @Override
    public ReplicaNodeStat getReplicaState(String topic, int partitionGroup) {
        ReplicaNodeStat replicaNodeStat= brokerStat.getOrCreateTopicStat(topic).getOrCreatePartitionGroupStat(partitionGroup).getReplicationStat().getStat();
        replicaNodeStat.setBrokerId(brokerStat.getBrokerId());
        return replicaNodeStat;
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
