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

import com.sun.management.GcInfo;
import org.joyqueue.broker.archive.ArchiveManager;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.election.ElectionService;
import org.joyqueue.broker.monitor.converter.BrokerMonitorConverter;
import org.joyqueue.broker.monitor.service.BrokerMonitorInternalService;
import org.joyqueue.broker.monitor.stat.BrokerStat;
import org.joyqueue.broker.monitor.stat.BrokerStatExt;
import org.joyqueue.broker.monitor.stat.ConsumerPendingStat;
import org.joyqueue.broker.monitor.stat.JVMStat;
import org.joyqueue.broker.monitor.stat.PartitionGroupPendingStat;
import org.joyqueue.broker.monitor.stat.PartitionGroupStat;
import org.joyqueue.broker.monitor.stat.PartitionStat;
import org.joyqueue.broker.monitor.stat.TopicPendingStat;
import org.joyqueue.broker.monitor.stat.TopicStat;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.monitor.BrokerMonitorInfo;
import org.joyqueue.monitor.BrokerStartupInfo;
import org.joyqueue.monitor.ElectionMonitorInfo;
import org.joyqueue.monitor.NameServerMonitorInfo;
import org.joyqueue.monitor.StoreMonitorInfo;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.nsr.NameService;
import org.joyqueue.store.PartitionGroupStore;
import org.joyqueue.store.StoreManagementService;
import org.joyqueue.store.StoreService;
import org.joyqueue.toolkit.format.Format;
import org.joyqueue.toolkit.lang.Online;
import org.joyqueue.toolkit.vm.DefaultGCNotificationParser;
import org.joyqueue.toolkit.vm.GCEvent;
import org.joyqueue.toolkit.vm.GCEventListener;
import org.joyqueue.toolkit.vm.GCEventType;
import org.joyqueue.toolkit.vm.GarbageCollectorMonitor;
import org.joyqueue.toolkit.vm.JVMMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.List;
import java.util.Map;

/**
 * BrokerMonitorInternalService
 * <p>
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
    private JVMMonitorService jvmMonitorService;
    private ArchiveManager archiveManager;
    private DefaultGCNotificationParser gcNotificationParser;


    public DefaultBrokerMonitorInternalService(BrokerStat brokerStat, Consume consume,
                                               StoreManagementService storeManagementService,
                                               NameService nameService, StoreService storeService,
                                               ElectionService electionManager, ClusterManager clusterManager, BrokerStartupInfo brokerStartupInfo, ArchiveManager archiveManager) {
        this.brokerStat = brokerStat;
        this.consume = consume;
        this.storeManagementService = storeManagementService;
        this.nameService = nameService;
        this.storeService = storeService;
        this.electionService = electionManager;
        this.clusterManager = clusterManager;
        this.brokerStartupInfo = brokerStartupInfo;
        this.jvmMonitorService = new GarbageCollectorMonitor();
        this.gcNotificationParser = new DefaultGCNotificationParser();
        this.gcNotificationParser.addListener(new DefaultGCEventListener(brokerStat.getJvmStat()));
        this.jvmMonitorService.addGCEventListener(gcNotificationParser);
        this.archiveManager = archiveManager;

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

    // BrokerStatExt里所有对象单独生成bean，不能复用monitor的bean
    @Override
    public BrokerStatExt getExtendBrokerStat(long timeStamp) {
        BrokerStatExt statExt = new BrokerStatExt(brokerStat);
        statExt.setTimeStamp(timeStamp);
        getJVMState(); // update current jvm state and memory stat
        statExt.getBrokerStat().getJvmStat().getRecentSnapshot();
        statExt.setTimeStamp(timeStamp);
        brokerStat.getJvmStat().setMemoryStat(jvmMonitorService.memSnapshot());
        Map<String, TopicPendingStat> topicPendingStatMap = statExt.getTopicPendingStatMap();

        for (TopicConfig topic : clusterManager.getTopics()) {
            TopicStat topicStat = brokerStat.getOrCreateTopicStat(topic.getName().getFullName());
            TopicPendingStat topicPendingStat = new TopicPendingStat();
            topicPendingStat.setTopic(topic.getName().getFullName());
            topicPendingStatMap.put(topic.getName().getFullName(), topicPendingStat);

            long storageSize = 0;
            List<PartitionGroupStore> partitionGroupStores = storeService.getStore(topicStat.getTopic());
            for (PartitionGroupStore pgStore : partitionGroupStores) {
                storageSize += pgStore.getTotalPhysicalStorageSize();
            }
            topicStat.setStoreSize(storageSize);

            long topicPending = 0;
            List<org.joyqueue.domain.Consumer> consumers = clusterManager.getLocalConsumersByTopic(topic.getName());

            for (org.joyqueue.domain.Consumer consumer : consumers) {
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
                        PartitionStat stat = new PartitionStat(consumer.getTopic().getFullName(),consumer.getApp(),partitionMetric.getPartition());
                        stat.setAckIndex(ackIndex);
                        stat.setRight(partitionMetric.getRightIndex());
                        partitionGroupPendingStat.getPartitionStatHashMap().put(partitionMetric.getPartition(),stat);
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
        // replicas lag state
        snapshotReplicaLag();
        // runtime memory usage state
        runtimeMemoryUsageState(statExt);
        runtimeStorageOccupy(brokerStat);
        statExt.setArchiveConsumePending(archiveManager.getConsumeBacklogNum());
        statExt.setArchiveProducePending(archiveManager.getSendBacklogNum());
        statExt.setTopicArchiveProducePending(archiveManager.getSendBacklogNumByTopic());
        return statExt;
    }

    /**
     * Replica log max position snapshots
     **/
    public void snapshotReplicaLag() {
        Map<String, TopicStat> topicStatMap = brokerStat.getTopicStats();
        for (TopicStat topicStat : topicStatMap.values()) {
            Map<Integer, PartitionGroupStat> partitionGroupStatMap = topicStat.getPartitionGroupStatMap();
            for (PartitionGroupStat partitionGroupStat : partitionGroupStatMap.values()) {
                StoreManagementService.PartitionGroupMetric partitionGroupMetric = storeManagementService.partitionGroupMetric(partitionGroupStat.getTopic(), partitionGroupStat.getPartitionGroup());
                if (partitionGroupMetric != null) {
                    partitionGroupStat.getReplicationStat().setMaxLogPosition(partitionGroupMetric.getRightPosition());
                }
            }
        }
    }

    /**
     *  GC event listener
     *
     **/
    public class DefaultGCEventListener implements GCEventListener {

        private JVMStat jvmStat;

        public DefaultGCEventListener(JVMStat jvmStat) {
            this.jvmStat = jvmStat;
        }

        @Override
        public void handleNotification(GCEvent event) {
            GcInfo gcInfo = event.getGcInfo().getGcInfo();
            if (event.getType() == GCEventType.END_OF_MAJOR || event.getType() == GCEventType.END_OF_MINOR) {
                jvmStat.getTotalGcTime().addAndGet(gcInfo.getDuration());
                jvmStat.getTotalGcTimes().incrementAndGet();
            }
            if (event.getType() == GCEventType.END_OF_MAJOR) {
                jvmStat.getOldGcTimes().mark(gcInfo.getDuration(), 1);
            } else if (event.getType() == GCEventType.END_OF_MINOR) {
                jvmStat.getEdenGcTimes().mark(gcInfo.getDuration(), 1);
            }
        }
    }


    /**
     * fill heap and non-heap memory usage state of current
     **/
    public void runtimeMemoryUsageState(BrokerStatExt brokerStatExt) {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        brokerStatExt.setHeap(memoryMXBean.getHeapMemoryUsage());
        brokerStatExt.setNonHeap(memoryMXBean.getNonHeapMemoryUsage());
    }

    @Override
    public JVMStat getJVMState() {
        JVMStat jvmStat = brokerStat.getJvmStat();
        jvmStat.setMemoryStat(jvmMonitorService.memSnapshot());
        return jvmStat;
    }

    @Override
    public BrokerStartupInfo getStartInfo() {
        return brokerStartupInfo;
    }

    @Override
    public void addGcEventListener(GCEventListener listener) {
        this.gcNotificationParser.addListener(listener);
    }


    /**
     * store storage size
     **/
    public void runtimeStorageOccupy(BrokerStat stat) {
        double totalSpace = storeManagementService.totalSpace();
        double freeSpace = storeManagementService.freeSpace();
        int percentage = (int) ((1 - freeSpace / totalSpace) * 100);
        stat.setStoragePercent(percentage);
    }
}