package org.joyqueue.broker.cluster;

import com.google.common.collect.Maps;
import org.joyqueue.broker.cluster.entry.ClusterNode;
import org.joyqueue.broker.event.BrokerEventBus;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.event.NameServerEvent;
import org.joyqueue.nsr.NameService;
import org.joyqueue.nsr.event.RemovePartitionGroupEvent;
import org.joyqueue.nsr.event.UpdatePartitionGroupEvent;
import org.joyqueue.store.StoreNode;
import org.joyqueue.store.StoreNodes;
import org.joyqueue.store.event.StoreNodeChangeEvent;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

/**
 * ClusterStoreManager
 * author: gaohaoxiang
 * date: 2020/3/23
 */
public class ClusterNodeManager extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(ClusterNodeManager.class);

    private Broker broker;
    private NameService nameService;
    private BrokerEventBus eventBus;

    private ConcurrentMap<String /** topic **/, ConcurrentMap<Integer /** group **/, ClusterNode>> nodeMap = Maps.newConcurrentMap();

    public ClusterNodeManager(NameService nameService, BrokerEventBus eventBus) {
        this.nameService = nameService;
        this.eventBus = eventBus;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    @Override
    protected void validate() throws Exception {
        this.eventBus.addListener(new ClusterNodeListener());
        this.nameService.addListener(new TopicGroupListener());
    }

    public ClusterNode getTopicGroupNode(String topic, int group) {
        ConcurrentMap<Integer, ClusterNode> groupMap = nodeMap.get(topic);
        if (groupMap == null) {
            return null;
        }
        return groupMap.get(group);
    }

    protected void updateTopicGroupNode(String topic, int group, StoreNodes storeNodes) {
        StoreNode wrNode = storeNodes.getRWNode();
        if (storeNodes.getRWNode() == null) {
            setTopicGroupNode(topic, group, new ClusterNode(-1));
            logger.info("update topic group node, topic: {}, group: {}, leader: {}", topic, group, -1);
        } else {
            setTopicGroupNode(topic, group, new ClusterNode(wrNode.getId()));
            logger.info("update topic group node, topic: {}, group: {}, leader: {}", topic, group, wrNode.getId());
        }
    }

    protected void setTopicGroupNode(String topic, int group, ClusterNode clusterNode) {
        ConcurrentMap<Integer, ClusterNode> groupMap = getOrCreateGroupMap(topic);
        groupMap.put(group, clusterNode);
    }

    protected void removeTopicGroupNode(String topic, int group) {
        ConcurrentMap<Integer, ClusterNode> groupMap = nodeMap.get(topic);
        if (groupMap == null) {
            return;
        }
        logger.info("remove topic group node, topic: {}, group: {}, leader: {}", topic, group, groupMap.get(group));
        groupMap.remove(group);
        if (groupMap.isEmpty()) {
            nodeMap.remove(topic);
        }
    }

    protected ConcurrentMap<Integer, ClusterNode> getOrCreateGroupMap(String topic) {
        ConcurrentMap<Integer, ClusterNode> groupMap = nodeMap.get(topic);
        if (groupMap == null) {
            groupMap = Maps.newConcurrentMap();
            ConcurrentMap<Integer, ClusterNode> oldGroupMap = nodeMap.putIfAbsent(topic, groupMap);
            if (oldGroupMap != null) {
                groupMap = oldGroupMap;
            }
        }
        return groupMap;
    }

    private class TopicGroupListener implements EventListener<NameServerEvent> {

        @Override
        public void onEvent(NameServerEvent event) {
            MetaEvent metaEvent = event.getMetaEvent();
            switch (event.getEventType()) {
                case UPDATE_PARTITION_GROUP: {
                    UpdatePartitionGroupEvent updatePartitionGroupEvent = (UpdatePartitionGroupEvent) metaEvent;
                    PartitionGroup newPartitionGroup = updatePartitionGroupEvent.getNewPartitionGroup();
                    if (!newPartitionGroup.getReplicas().contains(broker.getId())) {
                        removeTopicGroupNode(newPartitionGroup.getTopic().getFullName(), newPartitionGroup.getGroup());
                    }
                    break;
                }
                case REMOVE_PARTITION_GROUP: {
                    RemovePartitionGroupEvent removePartitionGroupEvent = (RemovePartitionGroupEvent) metaEvent;
                    PartitionGroup partitionGroup = removePartitionGroupEvent.getPartitionGroup();
                    removeTopicGroupNode(partitionGroup.getTopic().getFullName(), partitionGroup.getGroup());
                    break;
                }
            }
        }
    }

    private class ClusterNodeListener implements EventListener {

        @Override
        public void onEvent(Object event) {
            if (event instanceof StoreNodeChangeEvent) {
                StoreNodeChangeEvent storeNodeChangeEvent = (StoreNodeChangeEvent) event;
                updateTopicGroupNode(storeNodeChangeEvent.getTopic(), storeNodeChangeEvent.getGroup(), storeNodeChangeEvent.getNodes());
            }
        }
    }
}