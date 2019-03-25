package com.jd.journalq.store;

import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.store.replication.ReplicableStore;
import com.jd.journalq.store.transaction.TransactionStore;

import java.io.IOException;
import java.util.List;


/**
 * Broker 存储元数据服务
 */
public interface StoreService {
    boolean partitionGroupExists(String topic, int partitionGroup);

    boolean topicExists(String topic);

    /**
     * 获取事务消息使用的TransactionStore。
     * @return 如果Store存在则直接返回；
     * 如果Topic存在，Store目录文件不存在则自动创建Store；
     * 如果Topic不存在，返回null
     */
    TransactionStore getTransactionStore(String topic);
    List<TransactionStore> getAllTransactionStores();

    void removePartitionGroup(String topic, int partitionGroup);

    /**
     * 从文件恢复partition group，系统启动时调用
     */
    void restorePartitionGroup(String topic, int partitionGroup) throws Exception;

    /**
     * 创建PartitionGroup。仅当topic创建或者向topic中添加partitionGroup的时候调用，需要提供节点信息。
     * @param brokerIds 集群包含的节点（brokerId）数组
     */
    void createPartitionGroup(String topic, int partitionGroup, short[] partitions, int[] brokerIds) throws Exception;

    PartitionGroupStore getStore(String topic, int partitionGroup, QosLevel writeQosLevel);
    PartitionGroupStore getStore(String topic, int partitionGroup);
    List<PartitionGroupStore> getStore(String topic);

    /**
     * 变更PartitionGroup中的partition
     * @throws NoSuchPartitionGroupException PartitionGroup不存在时抛出此异常
     * @throws IOException 创建/删除Partition时读写文件异常时抛出
     */
    void rePartition(String topic, int partitionGroup, Short[] partitions) throws IOException;

    /**
     * 获取用于选举复制的存储
     */
    ReplicableStore getReplicableStore(String topic, int partitionGroup);

    /**
     * 获取管理接口
     */
    StoreManagementService getManageService();



}
