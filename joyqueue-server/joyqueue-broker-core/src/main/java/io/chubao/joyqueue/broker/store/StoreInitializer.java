package io.chubao.joyqueue.broker.store;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Shorts;
import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.config.BrokerStoreConfig;
import io.chubao.joyqueue.broker.election.ElectionService;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.nsr.event.AddPartitionGroupEvent;
import io.chubao.joyqueue.nsr.event.AddTopicEvent;
import io.chubao.joyqueue.nsr.event.LeaderChangeEvent;
import io.chubao.joyqueue.nsr.event.RemovePartitionGroupEvent;
import io.chubao.joyqueue.nsr.event.RemoveTopicEvent;
import io.chubao.joyqueue.nsr.event.UpdatePartitionGroupEvent;
import io.chubao.joyqueue.store.StoreService;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
import io.chubao.joyqueue.toolkit.service.Service;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * StoreInitializer
 * author: gaohaoxiang
 * date: 2019/8/28
 */
// TODO 如果启动后元数据和选举不一致，以元数据为准
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

    @Override
    protected void doStop() {
    }

    protected void restore() {
        Broker broker = clusterManager.getBroker();
        List<Replica> replicas = nameService.getReplicaByBroker(broker.getId());
        if (CollectionUtils.isEmpty(replicas)) {
            return;
        }

        for (Replica replica : replicas) {
            PartitionGroup group = clusterManager.getPartitionGroupByGroup(replica.getTopic(),replica.getGroup());
            if (group == null) {
                logger.warn("group is null topic {},replica {}", replica.getTopic(), replica.getGroup());
                throw new RuntimeException(String.format("group is null topic %s,replica %s", replica.getTopic(), replica.getGroup()));
            }
            if (!group.getReplicas().contains(broker.getId())) {
                continue;
            }
            doRestore(group, replica, broker);
        }
    }

    protected void doRestore(PartitionGroup group, Replica replica, Broker broker) {
        logger.info("begin restore topic {},group.no {} group {}",replica.getTopic().getFullName(),replica.getGroup(),group);

        if (!config.getForceRestore()) {
            storeService.restorePartitionGroup(group.getTopic().getFullName(), group.getGroup());
            return;
        }

        if (storeService.partitionGroupExists(group.getTopic().getFullName(), group.getGroup())) {
            storeService.restorePartitionGroup(group.getTopic().getFullName(), group.getGroup());
        } else {
            logger.warn("store not found, createPartitionGroup, topic {},group.no {} group {}",replica.getTopic().getFullName(),replica.getGroup(),group);
            storeService.createPartitionGroup(replica.getTopic().getFullName(), group.getGroup(), Shorts.toArray(group.getPartitions()));
            // TODO 有问题
//            try {
//                Map<Integer, Broker> newBrokers = Maps.newHashMap(group.getBrokers());
//                newBrokers.put(broker.getId(), broker);
//                electionService.onPartitionGroupCreate(group.getElectType(), group.getTopic(), group.getGroup(),
//                        new ArrayList<>(newBrokers.values()), group.getLearners(), broker.getId(), group.getLeader());
//            } catch (ElectionException e) {
//                logger.info("election create partitionGroup exception, topic {},group.no {} group {}",replica.getTopic().getFullName(),replica.getGroup(),group, e);
//            }
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
            }
        } catch (Exception e) {
            logger.error("onEvent exception, event: {}", event, e);
            throw new RuntimeException(e);
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
        int currentBrokerId = clusterManager.getBrokerId();
        Set<Integer> newReplicas = Sets.newHashSet(newPartitionGroup.getReplicas());
        Set<Integer> oldReplicas = Sets.newHashSet(oldPartitionGroup.getReplicas());
        newReplicas.removeAll(oldReplicas);
        oldReplicas.removeAll(newReplicas);

        List<Broker> brokers = Lists.newLinkedList();
        for (Integer newReplica : newReplicas) {
            brokers.add(nameService.getBroker(newReplica));
        }

        for (Integer newReplica : newPartitionGroup.getReplicas()) {
            if (!oldPartitionGroup.getReplicas().contains(newReplica)) {
                if (newReplica.equals(currentBrokerId)) {
                    logger.info("topic[{}] add partitionGroup[{}]", topicName, newPartitionGroup.getGroup());
                    storeService.createPartitionGroup(topicName.getFullName(), newPartitionGroup.getGroup(), Shorts.toArray(newPartitionGroup.getPartitions()));
                    electionService.onPartitionGroupCreate(newPartitionGroup.getElectType(), topicName, newPartitionGroup.getGroup(),
                            brokers, newPartitionGroup.getLearners(), clusterManager.getBrokerId(), newPartitionGroup.getLeader());
                } else {
                    logger.info("topic[{}] update partitionGroup[{}] add node[{}] ", topicName, newPartitionGroup.getGroup(), newReplica);
                    electionService.onNodeAdd(topicName, newPartitionGroup.getGroup(), newPartitionGroup.getElectType(),
                            brokers, newPartitionGroup.getLearners(), clusterManager.getBrokerById(newReplica),
                            currentBrokerId, newPartitionGroup.getLeader());
                    storeService.rePartition(topicName.getFullName(), newPartitionGroup.getGroup(), newPartitionGroup.getPartitions().toArray(new Short[newPartitionGroup.getPartitions().size()]));
                }
            }
        }

        if (oldPartitionGroup.getPartitions().size() != newPartitionGroup.getPartitions().size()) {
            storeService.rePartition(topicName.getFullName(), newPartitionGroup.getGroup(), newPartitionGroup.getPartitions().toArray(new Short[newPartitionGroup.getPartitions().size()]));
        }

        for (Integer oldReplica : oldPartitionGroup.getReplicas()) {
            if (!newPartitionGroup.getReplicas().contains(oldReplica)) {
                if (oldReplica.equals(currentBrokerId)) {
                    logger.info("topic[{}] add partitionGroup[{}]", topicName, newPartitionGroup.getGroup());
                    storeService.createPartitionGroup(topicName.getFullName(), newPartitionGroup.getGroup(), Shorts.toArray(newPartitionGroup.getPartitions()));
                    electionService.onPartitionGroupCreate(newPartitionGroup.getElectType(), topicName, newPartitionGroup.getGroup(),
                            brokers, newPartitionGroup.getLearners(), clusterManager.getBrokerId(), newPartitionGroup.getLeader());
                } else {
                    logger.info("topic[{}] update partitionGroup[{}] add node[{}] ", topicName, newPartitionGroup.getGroup(), oldReplica);
                    electionService.onNodeAdd(topicName, newPartitionGroup.getGroup(), newPartitionGroup.getElectType(),
                            brokers, newPartitionGroup.getLearners(), clusterManager.getBrokerById(oldReplica),
                            currentBrokerId, newPartitionGroup.getLeader());
                    storeService.rePartition(topicName.getFullName(), newPartitionGroup.getGroup(), newPartitionGroup.getPartitions().toArray(new Short[newPartitionGroup.getPartitions().size()]));
                }
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
