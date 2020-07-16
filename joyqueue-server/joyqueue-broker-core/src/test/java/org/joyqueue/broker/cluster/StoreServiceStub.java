package org.joyqueue.broker.cluster;

import org.joyqueue.monitor.BufferPoolMonitorInfo;
import org.joyqueue.nsr.NameService;
import org.joyqueue.store.PartitionGroupStore;
import org.joyqueue.store.StoreManagementService;
import org.joyqueue.store.StoreService;
import org.joyqueue.store.transaction.TransactionStore;
import org.joyqueue.toolkit.config.PropertySupplier;
import java.io.IOException;
import java.util.Collection;
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
    public List<TransactionStore> getAllTransactionStores() {
        return null;
    }

    @Override
    public void removePartitionGroup(String topic, int partitionGroup) {

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
    public StoreManagementService getManageService() {
        return null;
    }

    @Override
    public BufferPoolMonitorInfo monitorInfo() {
        return null;
    }

    @Override
    public TransactionStore getTransactionStore(String topic, short partition) {
        return null;
    }

    @Override
    public PartitionGroupStore restoreOrCreatePartitionGroup(String topic, int partitionGroup, short[] partitions, List<Integer> brokers, List<Integer> observers, int thisBrokerId, PropertySupplier extend) throws Exception {
        return null;
    }

    @Override
    public PartitionGroupStore createPartitionGroup(String topic, int partitionGroup, short[] partitions, List<Integer> brokers, List<Integer> observers, int thisBrokerId, PropertySupplier extend) throws Exception {
        return null;
    }

    @Override
    public void stopPartitionGroup(String topic, int partitionGroup) {

    }

    @Override
    public Collection<PartitionGroupStore> getAllStores() {
        return null;
    }

    @Override
    public void maybeRePartition(String topic, int partitionGroup, Collection<Short> expectPartitions) throws IOException {

    }

    @Override
    public void maybeUpdateReplicas(String topic, int partitionGroup, Collection<Integer> newReplicaBrokerIds) throws Exception {

    }

    @Override
    public void updatePreferredLeader(String topic, int partitionGroup, int brokerId) throws Exception {

    }

    @Override
    public List<TransactionStore> getTransactionStores(String topic) {
        return null;
    }

    @Override
    public String name() {
        return null;
    }
}