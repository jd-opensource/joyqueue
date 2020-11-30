package org.joyqueue.broker.monitor.service.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.broker.archive.ArchiveManager;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.election.DefaultElectionNode;
import org.joyqueue.broker.election.ElectionService;
import org.joyqueue.broker.election.FixLeaderElection;
import org.joyqueue.broker.election.LeaderElection;
import org.joyqueue.broker.election.RaftLeaderElection;
import org.joyqueue.broker.monitor.converter.BrokerMonitorConverter;
import org.joyqueue.broker.monitor.stat.AppStat;
import org.joyqueue.broker.monitor.stat.BrokerStat;
import org.joyqueue.broker.monitor.stat.ConnectionStat;
import org.joyqueue.broker.monitor.stat.DeQueueStat;
import org.joyqueue.broker.monitor.stat.EnQueueStat;
import org.joyqueue.broker.monitor.stat.PartitionGroupStat;
import org.joyqueue.broker.monitor.stat.PartitionStat;
import org.joyqueue.broker.monitor.stat.TopicStat;
import org.joyqueue.broker.replication.Replica;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.monitor.ArchiveMonitorInfo;
import org.joyqueue.monitor.BrokerMonitorInfo;
import org.joyqueue.monitor.BrokerMonitorInfoExt;
import org.joyqueue.monitor.PartitionGroupMonitorInfo;
import org.joyqueue.monitor.PartitionGroupNodeMonitorInfo;
import org.joyqueue.monitor.PartitionMonitorInfo;
import org.joyqueue.monitor.TopicMonitorInfo;
import org.joyqueue.store.StoreManagementService;

import java.util.List;
import java.util.Set;

/**
 * BrokerMonitorInfoExtService
 * author: gaohaoxiang
 * date: 2020/11/23
 */
public class BrokerMonitorInfoExtService {

    private Consume consume;
    private StoreManagementService storeManagementService;
    private ElectionService electionService;
    private ClusterManager clusterManager;
    private ArchiveManager archiveManager;
    private BrokerStat brokerStat;

    public BrokerMonitorInfoExtService(Consume consume, StoreManagementService storeManagementService, ElectionService electionService,
                                       ClusterManager clusterManager, ArchiveManager archiveManager, BrokerStat brokerStat) {
        this.consume = consume;
        this.storeManagementService = storeManagementService;
        this.electionService = electionService;
        this.clusterManager = clusterManager;
        this.archiveManager = archiveManager;
        this.brokerStat = brokerStat;
    }

    public BrokerMonitorInfoExt getBrokerInfoExt(BrokerMonitorInfo brokerMonitorInfo) {
        BrokerMonitorInfoExt brokerMonitorInfoExt = new BrokerMonitorInfoExt();
        brokerMonitorInfoExt.setConnection(brokerMonitorInfo.getConnection());
        brokerMonitorInfoExt.setDeQueue(brokerMonitorInfo.getDeQueue());
        brokerMonitorInfoExt.setEnQueue(brokerMonitorInfo.getEnQueue());
        brokerMonitorInfoExt.setReplication(brokerMonitorInfo.getReplication());
        brokerMonitorInfoExt.setStore(brokerMonitorInfo.getStore());
        brokerMonitorInfoExt.setNameServer(brokerMonitorInfo.getNameServer());
        brokerMonitorInfoExt.setElection(brokerMonitorInfo.getElection());
        brokerMonitorInfoExt.setBufferPoolMonitorInfo(brokerMonitorInfo.getBufferPoolMonitorInfo());
        brokerMonitorInfoExt.setStartupInfo(brokerMonitorInfo.getStartupInfo());
        brokerMonitorInfoExt.setTimestamp(brokerMonitorInfo.getTimestamp());
        brokerMonitorInfoExt.setTopics(generateTopics());
        brokerMonitorInfoExt.setAppTopics(generateAppTopics());
        brokerMonitorInfoExt.setPartitionGroups(generatePartitionGroups());
        brokerMonitorInfoExt.setAppPartitionGroups(generateAppPartitionGroups());
        brokerMonitorInfoExt.setPartitions(generatePartitions());
        brokerMonitorInfoExt.setAppPartitions(generateAppPartitions());
        brokerMonitorInfoExt.setArchive(generateArchive());
        return brokerMonitorInfoExt;
    }

    protected List<TopicMonitorInfo> generateTopics() {
        List<TopicMonitorInfo> result = Lists.newLinkedList();
        for (TopicConfig topic : clusterManager.getTopics()) {
            TopicMonitorInfo topicMonitorInfo = generateTopic(topic);
            result.add(topicMonitorInfo);
        }
        return result;
    }

    protected TopicMonitorInfo generateTopic(TopicConfig topic) {
        TopicStat topicStat = brokerStat.getOrCreateTopicStat(topic.getName().getFullName());
        return generateTopicMonitorInfo(topic.getName().getFullName(), topicStat.getConnectionStat(), topicStat.getEnQueueStat(), topicStat.getDeQueueStat());
    }

    protected List<TopicMonitorInfo> generateAppTopics() {
        List<TopicMonitorInfo> result = Lists.newLinkedList();
        for (TopicConfig topic : clusterManager.getTopics()) {
            List<Consumer> consumers = clusterManager.getLocalConsumersByTopic(topic.getName());
            List<Producer> producers = clusterManager.getLocalProducersByTopic(topic.getName());
            Set<String> apps = Sets.newHashSet();

            if (CollectionUtils.isNotEmpty(consumers)) {
                for (Consumer consumer : consumers) {
                    apps.add(consumer.getApp());
                }
            }
            if (CollectionUtils.isNotEmpty(producers)) {
                for (Producer producer : producers) {
                    apps.add(producer.getApp());

                }
            }

            for (String app : apps) {
                TopicMonitorInfo topicMonitorInfo = generateTopic(topic, app);
                topicMonitorInfo.setApp(app);
                result.add(topicMonitorInfo);
            }
        }
        return result;
    }

    protected TopicMonitorInfo generateTopic(TopicConfig topic, String app) {
        AppStat appStat = brokerStat.getOrCreateTopicStat(topic.getName().getFullName()).getOrCreateAppStat(app);
        TopicMonitorInfo topicMonitorInfo = generateTopicMonitorInfo(topic.getName().getFullName(), appStat.getConnectionStat(),
                appStat.getProducerStat().getEnQueueStat(), appStat.getConsumerStat().getDeQueueStat());

        if (clusterManager.tryGetConsumer(topic.getName(), app) != null) {
            long pending = 0;
            org.joyqueue.network.session.Consumer consumer = new org.joyqueue.network.session.Consumer();
            consumer.setTopic(topic.getName().getFullName());
            consumer.setApp(app);
            for (Short partition : clusterManager.getLocalPartitions(TopicName.parse(topicMonitorInfo.getTopic()))) {
                StoreManagementService.PartitionMetric partitionMetric = storeManagementService.partitionMetric(topic.getName().getFullName(), partition);
                if (partitionMetric == null) {
                    continue;
                }
                long ackIndex = Math.max(0, consume.getAckIndex(consumer, partition));
                pending += Math.max(0, partitionMetric.getRightIndex() - ackIndex);
            }
            topicMonitorInfo.setPending(pending);
        }

        return topicMonitorInfo;
    }

    protected TopicMonitorInfo generateTopicMonitorInfo(String topic, ConnectionStat connectionStat, EnQueueStat enQueueStat, DeQueueStat deQueueStat) {
        TopicMonitorInfo topicMonitorInfo = new TopicMonitorInfo();
        topicMonitorInfo.setTopic(topic);
        topicMonitorInfo.setConnection(BrokerMonitorConverter.convertConnectionMonitorInfo(connectionStat));
        topicMonitorInfo.setEnQueue(BrokerMonitorConverter.convertEnQueueMonitorInfo(enQueueStat));
        topicMonitorInfo.setDeQueue(BrokerMonitorConverter.convertDeQueueMonitorInfo(deQueueStat));
        return topicMonitorInfo;
    }

    protected List<PartitionGroupMonitorInfo> generatePartitionGroups() {
        List<PartitionGroupMonitorInfo> result = Lists.newLinkedList();
        for (TopicConfig topic : clusterManager.getTopics()) {
            for (PartitionGroup partitionGroup : clusterManager.getLocalPartitionGroups(topic)) {
                StoreManagementService.PartitionGroupMetric partitionGroupMetric = storeManagementService.partitionGroupMetric(partitionGroup.getTopic().getFullName(), partitionGroup.getGroup());
                if (partitionGroupMetric == null) {
                    continue;
                }
                PartitionGroupMonitorInfo partitionGroupMonitorInfo = generatePartitionGroupMonitorInfo(partitionGroup, partitionGroupMetric);
                result.add(partitionGroupMonitorInfo);
            }
        }
        return result;
    }

    protected PartitionGroupMonitorInfo generatePartitionGroupMonitorInfo(PartitionGroup partitionGroup, StoreManagementService.PartitionGroupMetric partitionGroupMetric) {
        PartitionGroupStat partitionGroupStat = brokerStat.getOrCreateTopicStat(partitionGroup.getTopic().getFullName()).getOrCreatePartitionGroupStat(partitionGroup.getGroup());
        PartitionGroupMonitorInfo partitionGroupMonitorInfo = generatePartitionGroupMonitorInfo(partitionGroup,
                partitionGroupMetric, partitionGroupStat.getEnQueueStat(), partitionGroupStat.getDeQueueStat());
        partitionGroupMonitorInfo.setReplication(BrokerMonitorConverter.convertReplicationMonitorInfo(partitionGroupStat.getReplicationStat()));

        LeaderElection leaderElection = electionService.getLeaderElection(partitionGroup.getTopic(), partitionGroup.getGroup());
        List<PartitionGroupNodeMonitorInfo> nodeInfos = Lists.newArrayList();
        if (leaderElection instanceof RaftLeaderElection) {
            RaftLeaderElection raftLeaderElection = (RaftLeaderElection) leaderElection;
            partitionGroupMonitorInfo.setTerm(raftLeaderElection.getCurrentTerm());

            for (Replica replica : raftLeaderElection.getReplicaGroup().getReplicas()) {
                PartitionGroupNodeMonitorInfo partitionGroupNodeMonitorInfo = new PartitionGroupNodeMonitorInfo();
                partitionGroupNodeMonitorInfo.setReplicaId(replica.replicaId());
                partitionGroupNodeMonitorInfo.setRightPosition(replica.writePosition());
                partitionGroupNodeMonitorInfo.setCommitPosition(replica.writePosition());
                partitionGroupNodeMonitorInfo.setPending(partitionGroupMetric.getRightPosition() - replica.writePosition());
                partitionGroupNodeMonitorInfo.setLastAppendTime(replica.getLastAppendTime());
                partitionGroupNodeMonitorInfo.setLastAppendSuccessTime(replica.lastAppendSuccessTime());
                partitionGroupNodeMonitorInfo.setLastReplicateConsumePosTime(replica.lastReplicateConsumePosTime());
                nodeInfos.add(partitionGroupNodeMonitorInfo);
            }
        } else if (leaderElection instanceof FixLeaderElection) {
            FixLeaderElection fixLeaderElection = (FixLeaderElection) leaderElection;
            for (DefaultElectionNode node : fixLeaderElection.getAllNodes()) {
                PartitionGroupNodeMonitorInfo partitionGroupNodeMonitorInfo = new PartitionGroupNodeMonitorInfo();
                partitionGroupNodeMonitorInfo.setReplicaId(node.getNodeId());
                partitionGroupNodeMonitorInfo.setCommitPosition(partitionGroupMetric.getRightPosition());
                partitionGroupNodeMonitorInfo.setRightPosition(partitionGroupMetric.getRightPosition());
                nodeInfos.add(partitionGroupNodeMonitorInfo);
            }
        }
        partitionGroupMonitorInfo.setNodeInfos(nodeInfos);
        return partitionGroupMonitorInfo;
    }

    protected List<PartitionGroupMonitorInfo> generateAppPartitionGroups() {
        List<PartitionGroupMonitorInfo> result = Lists.newLinkedList();
        for (TopicConfig topic : clusterManager.getTopics()) {
            for (PartitionGroup partitionGroup : clusterManager.getLocalPartitionGroups(topic)) {
                List<Consumer> consumers = clusterManager.getLocalConsumersByTopic(partitionGroup.getTopic());
                List<Producer> producers = clusterManager.getLocalProducersByTopic(partitionGroup.getTopic());
                StoreManagementService.PartitionGroupMetric partitionGroupMetric = storeManagementService.partitionGroupMetric(partitionGroup.getTopic().getFullName(), partitionGroup.getGroup());
                if (partitionGroupMetric == null) {
                    continue;
                }
                Set<String> apps = Sets.newHashSet();
                if (CollectionUtils.isNotEmpty(consumers)) {
                    for (Consumer consumer : consumers) {
                        apps.add(consumer.getApp());
                    }
                }
                if (CollectionUtils.isNotEmpty(producers)) {
                    for (Producer producer : producers) {
                        apps.add(producer.getApp());
                    }
                }
                for (String app : apps) {
                    PartitionGroupMonitorInfo partitionGroupMonitorInfo = generatePartitionGroupMonitorInfo(partitionGroup, partitionGroupMetric, app);
                    if (partitionGroupMonitorInfo == null) {
                        continue;
                    }
                    result.add(partitionGroupMonitorInfo);
                }
            }
        }
        return result;
    }

    protected PartitionGroupMonitorInfo generatePartitionGroupMonitorInfo(PartitionGroup partitionGroup,
                                                                          StoreManagementService.PartitionGroupMetric partitionGroupMetric, String app) {
        AppStat appStat = brokerStat.getOrCreateTopicStat(partitionGroup.getTopic().getFullName()).getOrCreateAppStat(app);
        PartitionGroupStat producerPartitionGroupStat = appStat.getProducerStat().getOrCreatePartitionGroupStat(partitionGroup.getGroup());
        PartitionGroupStat consumerPartitionGroupStat = appStat.getConsumerStat().getOrCreatePartitionGroupStat(partitionGroup.getGroup());
        PartitionGroupMonitorInfo partitionGroupMonitorInfo = generatePartitionGroupMonitorInfo(partitionGroup, partitionGroupMetric,
                producerPartitionGroupStat.getEnQueueStat(), consumerPartitionGroupStat.getDeQueueStat());
        partitionGroupMonitorInfo.setApp(app);

        if (clusterManager.tryGetConsumer(partitionGroup.getTopic(), app) != null) {
            long pending = 0;
            org.joyqueue.network.session.Consumer consumer = new org.joyqueue.network.session.Consumer();
            consumer.setTopic(partitionGroup.getTopic().getFullName());
            consumer.setApp(app);
            for (Short partition : partitionGroup.getPartitions()) {
                StoreManagementService.PartitionMetric partitionMetric = storeManagementService.partitionMetric(partitionGroup.getTopic().getFullName(), partition);
                if (partitionMetric == null) {
                    continue;
                }
                long ackIndex = Math.max(0, consume.getAckIndex(consumer, partition));
                pending += Math.max(0, partitionMetric.getRightIndex() - ackIndex);
            }
            partitionGroupMonitorInfo.setPending(pending);
        }
        return partitionGroupMonitorInfo;
    }

    protected PartitionGroupMonitorInfo generatePartitionGroupMonitorInfo(PartitionGroup partitionGroup,
                                                                          StoreManagementService.PartitionGroupMetric partitionGroupMetric, EnQueueStat enQueueStat, DeQueueStat deQueueStat) {
        PartitionGroupMonitorInfo partitionGroupMonitorInfo = new PartitionGroupMonitorInfo();
        partitionGroupMonitorInfo.setTopic(partitionGroup.getTopic().getFullName());
        partitionGroupMonitorInfo.setPartitionGroup(partitionGroup.getGroup());
        partitionGroupMonitorInfo.setLeftPosition(partitionGroupMetric.getLeftPosition());
        partitionGroupMonitorInfo.setRightPosition(partitionGroupMetric.getRightPosition());
        partitionGroupMonitorInfo.setIndexPosition(partitionGroupMetric.getIndexPosition());
        partitionGroupMonitorInfo.setFlushPosition(partitionGroupMetric.getFlushPosition());
        partitionGroupMonitorInfo.setReplicationPosition(partitionGroupMetric.getReplicationPosition());
        partitionGroupMonitorInfo.setEnQueue(BrokerMonitorConverter.convertEnQueueMonitorInfo(enQueueStat));
        partitionGroupMonitorInfo.setDeQueue(BrokerMonitorConverter.convertDeQueueMonitorInfo(deQueueStat));
        return partitionGroupMonitorInfo;
    }

    protected List<PartitionMonitorInfo> generatePartitions() {
        List<PartitionMonitorInfo> result = Lists.newLinkedList();
        for (TopicConfig topic : clusterManager.getTopics()) {
            for (PartitionGroup partitionGroup : clusterManager.getLocalPartitionGroups(topic)) {
                for (Short partition : partitionGroup.getPartitions()) {
                    StoreManagementService.PartitionMetric partitionMetric = storeManagementService.partitionMetric(topic.getName().getFullName(), partition);
                    if (partitionMetric == null) {
                        continue;
                    }
                    PartitionMonitorInfo partitionMonitorInfo = generatePartition(partitionGroup, partition, partitionMetric);
                    result.add(partitionMonitorInfo);
                }
            }
        }
        return result;
    }

    protected PartitionMonitorInfo generatePartition(PartitionGroup partitionGroup, short partition, StoreManagementService.PartitionMetric partitionMetric) {
        PartitionStat partitionStat = brokerStat.getOrCreateTopicStat(partitionGroup.getTopic().getFullName())
                .getOrCreatePartitionGroupStat(partitionGroup.getGroup()).getOrCreatePartitionStat(partition);

        PartitionMonitorInfo appPartitionMonitorInfo = new PartitionMonitorInfo();
        appPartitionMonitorInfo.setTopic(partitionGroup.getTopic().getFullName());
        appPartitionMonitorInfo.setPartitionGroup(partitionGroup.getGroup());
        appPartitionMonitorInfo.setPartition(partition);
        appPartitionMonitorInfo.setLeftIndex(partitionMetric.getLeftIndex());
        appPartitionMonitorInfo.setRightIndex(partitionMetric.getRightIndex());
        appPartitionMonitorInfo.setEnQueue(BrokerMonitorConverter.convertEnQueueMonitorInfo(partitionStat.getEnQueueStat()));
        appPartitionMonitorInfo.setDeQueue(BrokerMonitorConverter.convertDeQueueMonitorInfo(partitionStat.getDeQueueStat()));
        return appPartitionMonitorInfo;
    }

    protected List<PartitionMonitorInfo> generateAppPartitions() {
        List<PartitionMonitorInfo> result = Lists.newLinkedList();
        for (TopicConfig topic : clusterManager.getTopics()) {
            for (PartitionGroup partitionGroup : clusterManager.getLocalPartitionGroups(topic)) {
                for (Short partition : partitionGroup.getPartitions()) {
                    StoreManagementService.PartitionMetric partitionMetric = storeManagementService.partitionMetric(topic.getName().getFullName(), partition);
                    if (partitionMetric == null) {
                        continue;
                    }
                    for (Consumer consumer : clusterManager.getLocalConsumersByTopic(topic.getName())) {
                        PartitionMonitorInfo appPartitionMonitorInfo = generateAppPartition(partitionGroup, partition, partitionMetric, consumer.getApp());
                        result.add(appPartitionMonitorInfo);
                    }
                }
            }
        }
        return result;
    }

    protected PartitionMonitorInfo generateAppPartition(PartitionGroup partitionGroup, short partition, StoreManagementService.PartitionMetric partitionMetric, String app) {
        org.joyqueue.network.session.Consumer sessionConsumer = new org.joyqueue.network.session.Consumer();
        sessionConsumer.setTopic(partitionGroup.getTopic().getFullName());
        sessionConsumer.setApp(app);
        long ackIndex = Math.max(0, consume.getAckIndex(sessionConsumer, partition));
        AppStat appStat = brokerStat.getOrCreateTopicStat(partitionGroup.getTopic().getFullName()).getOrCreateAppStat(app);
        PartitionStat producerPartitionStat = appStat.getProducerStat().getOrCreatePartitionGroupStat(partitionGroup.getGroup()).getOrCreatePartitionStat(partition);
        PartitionStat consumerPartitionStat = appStat.getConsumerStat().getOrCreatePartitionGroupStat(partitionGroup.getGroup()).getOrCreatePartitionStat(partition);

        PartitionMonitorInfo appPartitionMonitorInfo = new PartitionMonitorInfo();
        appPartitionMonitorInfo.setTopic(partitionGroup.getTopic().getFullName());
        appPartitionMonitorInfo.setApp(app);
        appPartitionMonitorInfo.setPartitionGroup(partitionGroup.getGroup());
        appPartitionMonitorInfo.setPartition(partition);
        appPartitionMonitorInfo.setLeftIndex(partitionMetric.getLeftIndex());
        appPartitionMonitorInfo.setRightIndex(partitionMetric.getRightIndex());
        appPartitionMonitorInfo.setAckIndex(partitionMetric.getRightIndex());
        appPartitionMonitorInfo.setPending(Math.max(0, partitionMetric.getRightIndex() - ackIndex));
        appPartitionMonitorInfo.setEnQueue(BrokerMonitorConverter.convertEnQueueMonitorInfo(producerPartitionStat.getEnQueueStat()));
        appPartitionMonitorInfo.setDeQueue(BrokerMonitorConverter.convertDeQueueMonitorInfo(consumerPartitionStat.getDeQueueStat()));
        return appPartitionMonitorInfo;
    }

    protected ArchiveMonitorInfo generateArchive() {
        ArchiveMonitorInfo archiveMonitorInfo = new ArchiveMonitorInfo();
        archiveMonitorInfo.setConsumeBacklog(archiveManager.getConsumeBacklogNum());
        archiveMonitorInfo.setProduceBacklog(archiveManager.getSendBacklogNum());
        archiveMonitorInfo.setTopicProduceBacklog(archiveMonitorInfo.getTopicProduceBacklog());
        return archiveMonitorInfo;
    }
}