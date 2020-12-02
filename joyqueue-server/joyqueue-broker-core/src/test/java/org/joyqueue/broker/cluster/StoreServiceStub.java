package org.joyqueue.broker.cluster;

import com.google.common.collect.Lists;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.QosLevel;
import org.joyqueue.domain.TopicName;
import org.joyqueue.monitor.BufferPoolMonitorInfo;
import org.joyqueue.nsr.NameService;
import org.joyqueue.store.PartitionGroupStore;
import org.joyqueue.store.RemovedPartitionGroupStore;
import org.joyqueue.store.StoreManagementService;
import org.joyqueue.store.StoreNode;
import org.joyqueue.store.StoreNodes;
import org.joyqueue.store.StoreService;
import org.joyqueue.store.event.StoreEvent;
import org.joyqueue.store.replication.ReplicableStore;
import org.joyqueue.store.transaction.TransactionStore;
import org.joyqueue.toolkit.concurrent.EventListener;

import java.io.IOException;
import java.util.List;

/**
 * StoreServiceStub
 * author: gaohaoxiang
 * date: 2020/3/27
 */
public class StoreServiceStub implements StoreService {

    private NameService nameService;

    public StoreServiceStub(NameService nameService) {
        this.nameService = nameService;
    }

    @Override
    public boolean partitionGroupExists(String topic, int partitionGroup) {
        return false;
    }

    @Override
    public boolean topicExists(String topic) {
        return false;
    }

    @Override
    public TransactionStore getTransactionStore(String topic) {
        return null;
    }

    @Override
    public List<TransactionStore> getAllTransactionStores() {
        return null;
    }

    @Override
    public void removePartitionGroup(String topic, int partitionGroup) {

    }

    @Override
    public void physicalDeleteRemovedPartitionGroup(String topic, int partitionGroup) {

    }

    @Override
    public List<RemovedPartitionGroupStore> getRemovedPartitionGroups() {
        return null;
    }

    @Override
    public void restorePartitionGroup(String topic, int partitionGroup) {

    }

    @Override
    public void createPartitionGroup(String topic, int partitionGroup, short[] partitions) {

    }

    @Override
    public PartitionGroupStore getStore(String topic, int partitionGroup, QosLevel writeQosLevel) {
        return null;
    }

    @Override
    public PartitionGroupStore getStore(String topic, int partitionGroup) {
        return null;
    }

    @Override
    public List<PartitionGroupStore> getStore(String topic) {
        return null;
    }

    @Override
    public void rePartition(String topic, int partitionGroup, Short[] partitions) throws IOException {

    }

    @Override
    public ReplicableStore getReplicableStore(String topic, int partitionGroup) {
        return null;
    }

    @Override
    public StoreManagementService getManageService() {
        return null;
    }

    @Override
    public BufferPoolMonitorInfo monitorInfo() {
        return null;
    }

    @Override
    public StoreNodes getNodes(String topic, int group) {
        PartitionGroup partitionGroup = nameService.getTopicConfig(TopicName.parse(topic)).getPartitionGroups().get(group);
        if (partitionGroup == null) {
            return null;
        }
        List<StoreNode> nodes = Lists.newArrayList();
        for (Integer replica : partitionGroup.getReplicas()) {
            if (partitionGroup.getLeader().equals(replica)) {
                nodes.add(new StoreNode(replica, true, true));
            } else {
                nodes.add(new StoreNode(replica, false, false));
            }
        }
        return new StoreNodes(nodes);
    }

    @Override
    public void addListener(EventListener<StoreEvent> listener) {

    }

    @Override
    public void removeListener(EventListener<StoreEvent> listener) {

    }
}