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
package org.joyqueue.broker.store;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Shorts;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.cluster.event.CompensateEvent;
import org.joyqueue.broker.config.BrokerStoreConfig;
import org.joyqueue.broker.election.DefaultElectionNode;
import org.joyqueue.broker.election.ElectionService;
import org.joyqueue.broker.election.LeaderElection;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Replica;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.nsr.NameService;
import org.joyqueue.nsr.event.AddPartitionGroupEvent;
import org.joyqueue.nsr.event.AddTopicEvent;
import org.joyqueue.nsr.event.LeaderChangeEvent;
import org.joyqueue.nsr.event.RemovePartitionGroupEvent;
import org.joyqueue.nsr.event.RemoveTopicEvent;
import org.joyqueue.nsr.event.UpdatePartitionGroupEvent;
import org.joyqueue.store.PartitionGroupStore;
import org.joyqueue.store.StoreService;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.service.Service;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * StoreInitializer
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class StoreInitializer extends Service implements EventListener<MetaEvent> {

    protected static final Logger logger = LoggerFactory.getLogger(StoreInitializer.class);

    private BrokerStoreConfig config;
    private NameService nameService;
    private ClusterManager clusterManager;
    private StoreService storeService;
    private ElectionService electionService;

    public StoreInitializer(BrokerStoreConfig config, NameService nameService, ClusterManager clusterManager, StoreService storeService, ElectionService electionService) {
        this.config = config;
        this.nameService = nameService;
        this.clusterManager = clusterManager;
        this.storeService = storeService;
        this.electionService = electionService;
    }

    @Override
    protected void doStart() throws Exception {
        restore();
        clusterManager.addListener(this);
    }

    protected void restore() throws Exception {
        Broker broker = clusterManager.getBroker();
        List<Replica> replicas = nameService.getReplicaByBroker(broker.getId());
        if (CollectionUtils.isEmpty(replicas)) {
            return;
        }
        // 并行恢复所有的PartitionGroup
        ExecutorService executor = Executors.newFixedThreadPool(32, new NamedThreadFactory("Store-recover-threads"));
        try {
            CompletableFuture.allOf(
                    replicas.stream()
                            .map(replica -> CompletableFuture.runAsync(() -> {
                                try {
                                    PartitionGroup group = clusterManager.getPartitionGroupByGroup(replica.getTopic(), replica.getGroup());
                                    if (group == null) {
                                        logger.warn("group is null topic {},replica {}", replica.getTopic(), replica.getGroup());
                                        throw new RuntimeException(String.format("group is null topic %s,replica %s", replica.getTopic(), replica.getGroup()));
                                    }
                                    if (!group.getReplicas().contains(broker.getId())) {
                                        return;
                                    }
                                    doRestore(group, replica, broker);
                                } catch (Exception e) {
                                    throw new CompletionException(e);
                                }
                                    }, executor)
                            ).toArray(CompletableFuture[]::new)
            ).get();
        } finally {
            executor.shutdown();
        }
    }

    protected void doRestore(PartitionGroup group, Replica replica, Broker broker) throws Exception {
        if (config.getForceRestore()) { // 强制恢复，如果磁盘上没有这个PartitionGroup，则新建一个
            logger.info("force restore topic {}, group.no {} group {}", replica.getTopic().getFullName(), replica.getGroup(), group);
            if (storeService.partitionGroupExists(group.getTopic().getFullName(), group.getGroup())) {
                storeService.restorePartitionGroup(group.getTopic().getFullName(), group.getGroup());
            } else {
                logger.warn("create partitionGroup store, topic {},group.no {} group {}", replica.getTopic().getFullName(), replica.getGroup(),group);
                storeService.createPartitionGroup(replica.getTopic().getFullName(), group.getGroup(), Shorts.toArray(group.getPartitions()));
            }
        } else {
            logger.info("restore topic {}, group.no {} group {}", replica.getTopic().getFullName(), replica.getGroup(),group);
            storeService.restorePartitionGroup(group.getTopic().getFullName(), group.getGroup());
        }
    }

    @Override
    public void onEvent(MetaEvent event) {
        try {
            switch (event.getEventType()) {
                case ADD_TOPIC: {
                    AddTopicEvent addTopicEvent = (AddTopicEvent) event;
                    for (PartitionGroup partitionGroup : addTopicEvent.getPartitionGroups()) {
                        onAddPartitionGroup(addTopicEvent.getTopic().getName(), partitionGroup);
                    }
                    break;
                }
                case REMOVE_TOPIC: {
                    RemoveTopicEvent removeTopicEvent = (RemoveTopicEvent) event;
                    for (PartitionGroup partitionGroup : removeTopicEvent.getPartitionGroups()) {
                        onRemovePartitionGroup(removeTopicEvent.getTopic().getName(), partitionGroup);
                    }
                    break;
                }
                case ADD_PARTITION_GROUP: {
                    AddPartitionGroupEvent addPartitionGroupEvent = (AddPartitionGroupEvent) event;
                    onAddPartitionGroup(addPartitionGroupEvent.getTopic(), addPartitionGroupEvent.getPartitionGroup());
                    break;
                }
                case UPDATE_PARTITION_GROUP: {
                    UpdatePartitionGroupEvent updatePartitionGroupEvent = (UpdatePartitionGroupEvent) event;
                    onUpdatePartitionGroup(updatePartitionGroupEvent.getTopic(), updatePartitionGroupEvent.getOldPartitionGroup(), updatePartitionGroupEvent.getNewPartitionGroup());
                    break;
                }
                case REMOVE_PARTITION_GROUP: {
                    RemovePartitionGroupEvent removePartitionGroupEvent = (RemovePartitionGroupEvent) event;
                    onRemovePartitionGroup(removePartitionGroupEvent.getTopic(), removePartitionGroupEvent.getPartitionGroup());
                    break;
                }
                case LEADER_CHANGE: {
                    LeaderChangeEvent leaderChangeEvent = (LeaderChangeEvent) event;
                    onLeaderChange(leaderChangeEvent.getTopic(), leaderChangeEvent.getOldPartitionGroup(), leaderChangeEvent.getNewPartitionGroup());
                    break;
                }
                case COMPENSATE: {
                    CompensateEvent compensateEvent = (CompensateEvent) event;
//                    onCompensate(compensateEvent.getTopics());
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("onEvent exception, event: {}", event, e);
            throw new RuntimeException(e);
        }
    }

    protected void onCompensate(Map<TopicName, TopicConfig> topics) {
        for (Map.Entry<TopicName, TopicConfig> topicEntry : topics.entrySet()) {
            TopicConfig topicConfig = topicEntry.getValue();
            for (Map.Entry<Integer, PartitionGroup> groupEntry : topicConfig.getPartitionGroups().entrySet()) {
                PartitionGroup partitionGroup = groupEntry.getValue();
                PartitionGroupStore store = storeService.getStore(topicConfig.getName().getFullName(), partitionGroup.getGroup());
                LeaderElection leaderElection = electionService.getLeaderElection(topicConfig.getName(), partitionGroup.getGroup());

                try {
                    if (store == null) {
                        logger.info("store create partitionGroup, topic: {}, group: {}", topicConfig.getName(), partitionGroup.getGroup());
                        storeService.createPartitionGroup(topicConfig.getName().getFullName(), partitionGroup.getGroup(), Shorts.toArray(partitionGroup.getPartitions()));
                    } else {
                        if (!Arrays.equals(store.listPartitions(), partitionGroup.getPartitions().toArray(new Short[0]))) {
                            logger.info("store partition, topic: {}, group: {}, partition {} -> {}",
                                    topicConfig.getName(), partitionGroup.getGroup(), Arrays.toString(store.listPartitions()), partitionGroup.getPartitions());
                            storeService.rePartition(topicConfig.getName().getFullName(), partitionGroup.getGroup(), partitionGroup.getPartitions().toArray(new Short[0]));
                        }
                    }

                    if (leaderElection == null) {
                        List<Broker> brokers = Lists.newLinkedList();
                        for (Integer replica : partitionGroup.getReplicas()) {
                            brokers.add(nameService.getBroker(replica));
                        }
                        logger.info("store create partitionGroup, topic: {}, group: {}", topicConfig.getName(), partitionGroup.getGroup());
                        electionService.onPartitionGroupCreate(partitionGroup.getElectType(), topicConfig.getName(), partitionGroup.getGroup(),
                                brokers, partitionGroup.getLearners(), clusterManager.getBrokerId(), partitionGroup.getLeader());
                    } else {
                        for (DefaultElectionNode electionNode : leaderElection.getAllNodes()) {
                            if (!partitionGroup.getReplicas().contains(electionNode.getNodeId())) {
                                logger.info("election remove node, topic: {}, group: {}, replica: {}", topicConfig.getName(), partitionGroup.getGroup(), electionNode.getNodeId());
                                electionService.onNodeRemove(topicConfig.getName(), partitionGroup.getGroup(), electionNode.getNodeId(), clusterManager.getBrokerId());
                                storeService.rePartition(topicConfig.getName().getFullName(), partitionGroup.getGroup(), partitionGroup.getPartitions().toArray(new Short[0]));
                            }
                        }
                        for (Integer replica : partitionGroup.getReplicas()) {
                            boolean isExist = false;
                            for (DefaultElectionNode electionNode : leaderElection.getAllNodes()) {
                                if (electionNode.getNodeId() == replica) {
                                    isExist = true;
                                    break;
                                }
                            }

                            if (!isExist) {
                                List<Broker> brokers = Lists.newLinkedList();
                                for (Integer partitionGroupReplica : partitionGroup.getReplicas()) {
                                    brokers.add(nameService.getBroker(partitionGroupReplica));
                                }

                                logger.info("election add node, topic: {}, group: {}, replica: {}", topicConfig.getName(), partitionGroup.getGroup(), replica);
                                electionService.onNodeAdd(topicConfig.getName(), partitionGroup.getGroup(), partitionGroup.getElectType(),
                                        brokers, partitionGroup.getLearners(), nameService.getBroker(replica),
                                        clusterManager.getBrokerId(), partitionGroup.getLeader());
                                storeService.rePartition(topicConfig.getName().getFullName(), partitionGroup.getGroup(), partitionGroup.getPartitions().toArray(new Short[0]));
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("compensate exception, topic: {}, group: {}", topicConfig.getName(), partitionGroup.getGroup(), e);
                }
            }
        }

        List<LeaderElection> leaderElections = electionService.getLeaderElections();
        for (LeaderElection leaderElection : leaderElections) {
            TopicName topic = TopicName.parse(leaderElection.getTopicPartitionGroup().getTopic());
            int group = leaderElection.getTopicPartitionGroup().getPartitionGroupId();
            if (topics.containsKey(topic)) {
                continue;
            }
            try {
                logger.info("election remove partitionGroup, topic: {}, group: {}", topic, group);
                storeService.removePartitionGroup(topic.getFullName(), group);
                electionService.onPartitionGroupRemove(topic, group);
            } catch (Exception e) {
                logger.error("compensate exception, topic: {}, group: {}", topic, group, e);
            }
        }
    }

    protected void onAddPartitionGroup(TopicName topicName, PartitionGroup partitionGroup) throws Exception {
        logger.info("onAddPartitionGroup, topic: {}, partitionGroup: {}", topicName, partitionGroup);

        Set<Integer> replicas = partitionGroup.getReplicas();
        List<Broker> brokers = new ArrayList<>(replicas.size());
        replicas.forEach(brokerId -> {
            brokers.add(clusterManager.getBrokerById(brokerId));
        });
        storeService.createPartitionGroup(topicName.getFullName(), partitionGroup.getGroup(), Shorts.toArray(partitionGroup.getPartitions()));
        electionService.onPartitionGroupCreate(partitionGroup.getElectType(), partitionGroup.getTopic(), partitionGroup.getGroup(),
                brokers, partitionGroup.getLearners(), clusterManager.getBrokerId(), partitionGroup.getLeader());
    }

    protected void onUpdatePartitionGroup(TopicName topicName, PartitionGroup oldPartitionGroup, PartitionGroup newPartitionGroup) throws Exception {
        logger.info("onUpdatePartitionGroup, topic: {}, oldPartitionGroup: {}, newPartition: {}", topicName, oldPartitionGroup, newPartitionGroup);

        int currentBrokerId = clusterManager.getBrokerId();
        Set<Integer> newReplicas = Sets.newHashSet(newPartitionGroup.getReplicas());
        Set<Integer> oldReplicas = Sets.newHashSet(oldPartitionGroup.getReplicas());
        newReplicas.removeAll(oldReplicas);
        oldReplicas.removeAll(newReplicas);

        List<Broker> brokers = Lists.newLinkedList();
        for (Integer newReplica : newPartitionGroup.getReplicas()) {
            brokers.add(nameService.getBroker(newReplica));
        }

        for (Integer newReplica : newPartitionGroup.getReplicas()) {
            if (oldPartitionGroup.getReplicas().contains(newReplica)) {
                continue;
            }
            if (newReplica.equals(currentBrokerId)) {
                logger.info("topic[{}] add partitionGroup[{}]", topicName, newPartitionGroup.getGroup());
                storeService.createPartitionGroup(topicName.getFullName(), newPartitionGroup.getGroup(), Shorts.toArray(newPartitionGroup.getPartitions()));
                electionService.onPartitionGroupCreate(newPartitionGroup.getElectType(), topicName, newPartitionGroup.getGroup(),
                        brokers, newPartitionGroup.getLearners(), clusterManager.getBrokerId(), newPartitionGroup.getLeader());
            } else {
                logger.info("topic[{}] update partitionGroup[{}] add node[{}] ", topicName, newPartitionGroup.getGroup(), newReplica);
                electionService.onNodeAdd(topicName, newPartitionGroup.getGroup(), newPartitionGroup.getElectType(),
                        brokers, newPartitionGroup.getLearners(), nameService.getBroker(newReplica),
                        currentBrokerId, newPartitionGroup.getLeader());
                storeService.rePartition(topicName.getFullName(), newPartitionGroup.getGroup(), newPartitionGroup.getPartitions().toArray(new Short[newPartitionGroup.getPartitions().size()]));
            }
        }

        if (oldPartitionGroup.getPartitions().size() != newPartitionGroup.getPartitions().size()) {
            storeService.rePartition(topicName.getFullName(), newPartitionGroup.getGroup(), newPartitionGroup.getPartitions().toArray(new Short[newPartitionGroup.getPartitions().size()]));
        }

        for (Integer oldReplica : oldPartitionGroup.getReplicas()) {
            if (newPartitionGroup.getReplicas().contains(oldReplica)) {
                continue;
            }
            if (oldReplica.equals(currentBrokerId)) {
                logger.info("topic[{}] add partitionGroup[{}]", topicName, newPartitionGroup.getGroup());
                storeService.removePartitionGroup(topicName.getFullName(), newPartitionGroup.getGroup());
                electionService.onPartitionGroupRemove(topicName, newPartitionGroup.getGroup());
            } else {
                logger.info("topic[{}] update partitionGroup[{}] add node[{}] ", topicName, newPartitionGroup.getGroup(), oldReplica);
                electionService.onNodeRemove(topicName, newPartitionGroup.getGroup(), oldReplica, currentBrokerId);
                storeService.rePartition(topicName.getFullName(), newPartitionGroup.getGroup(), newPartitionGroup.getPartitions().toArray(new Short[newPartitionGroup.getPartitions().size()]));
            }
        }
    }

    protected void onRemovePartitionGroup(TopicName topicName, PartitionGroup partitionGroup) throws Exception {
        logger.info("onRemovePartitionGroup, topic: {}, partitionGroup: {}", topicName, partitionGroup);
        storeService.removePartitionGroup(topicName.getFullName(), partitionGroup.getGroup());
        electionService.onPartitionGroupRemove(topicName, partitionGroup.getGroup());
    }

    protected void onLeaderChange(TopicName topicName, PartitionGroup oldPartitionGroup, PartitionGroup newPartitionGroup) throws Exception {
        logger.info("onLeaderChange, topic: {}, partitionGroup: {}", topicName, newPartitionGroup);
        electionService.onLeaderChange(topicName, newPartitionGroup.getGroup(), newPartitionGroup.getLeader());
    }
}
