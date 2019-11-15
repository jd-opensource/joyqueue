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
package io.chubao.joyqueue.broker.monitor.service.support;

import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.consumer.Consume;
import io.chubao.joyqueue.broker.election.ElectionService;
import io.chubao.joyqueue.broker.monitor.converter.BrokerMonitorConverter;
import io.chubao.joyqueue.broker.monitor.service.BrokerMonitorInternalService;
import io.chubao.joyqueue.broker.monitor.stat.BrokerStat;
import io.chubao.joyqueue.broker.monitor.stat.BrokerStatExt;
import io.chubao.joyqueue.broker.monitor.stat.ConsumerPendingStat;
import io.chubao.joyqueue.broker.monitor.stat.PartitionGroupPendingStat;
import io.chubao.joyqueue.broker.monitor.stat.TopicPendingStat;
import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.monitor.BrokerMonitorInfo;
import io.chubao.joyqueue.monitor.BrokerStartupInfo;
import io.chubao.joyqueue.monitor.ElectionMonitorInfo;
import io.chubao.joyqueue.monitor.NameServerMonitorInfo;
import io.chubao.joyqueue.monitor.StoreMonitorInfo;
import io.chubao.joyqueue.network.session.Consumer;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.store.StoreManagementService;
import io.chubao.joyqueue.store.StoreService;
import io.chubao.joyqueue.toolkit.format.Format;
import io.chubao.joyqueue.toolkit.lang.Online;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.List;
import java.util.Map;

/**
 * BrokerMonitorInternalService
 *
 * author: gaohaoxiang
 * date: 2018/10/15
 */
public class DefaultBrokerMonitorInternalService implements BrokerMonitorInternalService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultBrokerMonitorInternalService.class);

    private BrokerStat brokerStat;
    private Consume consume;
    private StoreManagementService storeManagementService;
    private NameService nameService;
    private StoreService storeService;
    private ElectionService electionService;
    private ClusterManager clusterManager;
    private BrokerStartupInfo brokerStartupInfo;

    public DefaultBrokerMonitorInternalService(BrokerStat brokerStat, Consume consume,
                                               StoreManagementService storeManagementService,
                                               NameService nameService, StoreService storeService,
                                               ElectionService electionManager, ClusterManager clusterManager, BrokerStartupInfo brokerStartupInfo) {
        this.brokerStat = brokerStat;
        this.consume = consume;
        this.storeManagementService = storeManagementService;
        this.nameService = nameService;
        this.storeService = storeService;
        this.electionService = electionManager;
        this.clusterManager = clusterManager;
        this.brokerStartupInfo = brokerStartupInfo;
    }

    @Override
    public BrokerMonitorInfo getBrokerInfo() {
        BrokerMonitorInfo brokerMonitorInfo = new BrokerMonitorInfo();
        brokerMonitorInfo.setConnection(BrokerMonitorConverter.convertConnectionMonitorInfo(brokerStat.getConnectionStat()));
        brokerMonitorInfo.setEnQueue(BrokerMonitorConverter.convertEnQueueMonitorInfo(brokerStat.getEnQueueStat()));
        brokerMonitorInfo.setDeQueue(BrokerMonitorConverter.convertDeQueueMonitorInfo(brokerStat.getDeQueueStat()));
        brokerMonitorInfo.setReplication(BrokerMonitorConverter.convertReplicationMonitorInfo(brokerStat.getReplicationStat()));

        StoreMonitorInfo storeMonitorInfo = new StoreMonitorInfo();
        storeMonitorInfo.setStarted(storeService instanceof Online ? ((Online) storeService).isStarted() : true);
        storeMonitorInfo.setFreeSpace(Format.formatSize(storeManagementService.freeSpace()));
        storeMonitorInfo.setTotalSpace(Format.formatSize(storeManagementService.totalSpace()));

        NameServerMonitorInfo nameServerMonitorInfo = new NameServerMonitorInfo();
        nameServerMonitorInfo.setStarted(nameService.isStarted());

        ElectionMonitorInfo electionMonitorInfo = new ElectionMonitorInfo();
        boolean electionStarted = electionService instanceof Online ? ((Online) electionService).isStarted() : true;
        electionMonitorInfo.setStarted(electionStarted);

        brokerMonitorInfo.getReplication().setStarted(electionStarted);

        brokerMonitorInfo.setStore(storeMonitorInfo);
        brokerMonitorInfo.setNameServer(nameServerMonitorInfo);
        brokerMonitorInfo.setElection(electionMonitorInfo);

        brokerMonitorInfo.setBufferPoolMonitorInfo(storeService.monitorInfo());
        brokerMonitorInfo.setStartupInfo(brokerStartupInfo);
        return brokerMonitorInfo;
    }


    @Override
    public BrokerStatExt getExtendBrokerStat(long timeStamp) {
        BrokerStatExt statExt = new BrokerStatExt(brokerStat);
        statExt.setTimeStamp(timeStamp);
        Map<String, TopicPendingStat> topicPendingStatMap = statExt.getTopicPendingStatMap();

        for (TopicConfig topic : clusterManager.getTopics()) {
            TopicPendingStat topicPendingStat = new TopicPendingStat();
            topicPendingStat.setTopic(topic.getName().getFullName());
            topicPendingStatMap.put(topic.getName().getFullName(), topicPendingStat);

            long topicPending = 0;
            List<io.chubao.joyqueue.domain.Consumer> consumers = clusterManager.getLocalConsumersByTopic(topic.getName());

            for (io.chubao.joyqueue.domain.Consumer consumer : consumers) {
                long consumerPending = 0;
                ConsumerPendingStat consumerPendingStat = new ConsumerPendingStat();
                consumerPendingStat.setApp(consumer.getApp());
                consumerPendingStat.setTopic(consumer.getTopic().getFullName());
                Map<String, ConsumerPendingStat> consumerPendingStatMap = topicPendingStat.getPendingStatSubMap();
                consumerPendingStatMap.put(consumer.getApp(), consumerPendingStat);
                Map<Integer, PartitionGroupPendingStat> partitionGroupPendingStatMap = consumerPendingStat.getPendingStatSubMap();

                StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(consumer.getTopic().getFullName());
                for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
                    if (!clusterManager.isLeader(consumer.getTopic().getFullName(), partitionGroupMetric.getPartitionGroup())) {
                        continue;
                    }

                    long partitionGroupPending = 0;
                    int partitionGroupId = partitionGroupMetric.getPartitionGroup();
                    PartitionGroupPendingStat partitionGroupPendingStat = new PartitionGroupPendingStat();
                    partitionGroupPendingStat.setPartitionGroup(partitionGroupId);
                    partitionGroupPendingStat.setTopic(consumer.getTopic().getFullName());
                    partitionGroupPendingStat.setApp(consumer.getApp());
                    partitionGroupPendingStatMap.put(partitionGroupId, partitionGroupPendingStat);

                    for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                        long ackIndex = consume.getAckIndex(new Consumer(consumer.getTopic().getFullName(), consumer.getApp()), partitionMetric.getPartition());
                        if (ackIndex < 0) {
                            ackIndex = 0;
                        }
                        long partitionPending = partitionMetric.getRightIndex() - ackIndex;
                        Map<Short, Long> partitionPendStatMap = partitionGroupPendingStat.getPendingStatSubMap();
                        partitionPendStatMap.put(partitionMetric.getPartition(), partitionPending);
                        partitionGroupPending += partitionPending;
                    }
                    partitionGroupPendingStat.setPending(partitionGroupPending);
                    consumerPending += partitionGroupPending;
                }
                consumerPendingStat.setPending(consumerPending);
                topicPending += consumerPending;
            }
            topicPendingStat.setPending(topicPending);
        }

        runtimeMemoryUsageState(statExt);
        return statExt;
    }

    @Override
    public BrokerStartupInfo getStartInfo() {
        return brokerStartupInfo;
    }

    /**
     * fill heap and non-heap memory usage state of current
     *
     **/
    public void runtimeMemoryUsageState(BrokerStatExt brokerStatExt) {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        brokerStatExt.setHeap(memoryMXBean.getHeapMemoryUsage());
        brokerStatExt.setNonHeap(memoryMXBean.getNonHeapMemoryUsage());
    }
}