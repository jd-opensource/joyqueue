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
package org.joyqueue.store;

import org.joyqueue.domain.QosLevel;
import org.joyqueue.monitor.BufferPoolMonitorInfo;
import org.joyqueue.store.event.StoreEvent;
import org.joyqueue.store.replication.ReplicableStore;
import org.joyqueue.store.transaction.TransactionStore;
import org.joyqueue.toolkit.concurrent.EventListener;

import java.io.IOException;
import java.util.List;


/**
 * Broker 存储元数据服务
 */
public interface StoreService {
    /**
     * 判断Partition group 目录是否存在，
     * 以磁盘上的Partition group目录为准。
     * @param topic Topic
     * @param partitionGroup Partition group
     * @return 存在返回true， 否则返回false
     */
    boolean partitionGroupExists(String topic, int partitionGroup);

    /**
     * 判断Topic 目录是否存在，
     * 以磁盘上的Partition group目录为准。
     * @param topic Topic
     * @return 存在返回true， 否则返回false
     */
    boolean topicExists(String topic);

    /**
     * 获取事务消息使用的{@link TransactionStore}
     * @return 如果 {@link TransactionStore}存在则直接返回；
     * 如果Topic存在，{@link TransactionStore}目录文件不存在则自动创建 {@link TransactionStore}；
     * 如果Topic不存在，返回null。
     */
    TransactionStore getTransactionStore(String topic);

    /**
     * 获取全部{@link TransactionStore}
     * @return {@link TransactionStore}列表
     */
    List<TransactionStore> getAllTransactionStores();

    /**
     * 删除Partition group。此操作不会物理上删除Partition group所在的目录，而是将数据重名为“.d.[PartitionGroup]”。
     * @param topic Topic
     * @param partitionGroup Partition group
     */
    void removePartitionGroup(String topic, int partitionGroup);

    /**
     * 物理删除已删除的group
     * @param topic
     * @param partitionGroup
     */
    void physicalDeleteRemovedPartitionGroup(String topic, int partitionGroup);

    /**
     * 获取已删除的group
     * @return
     */
    List<RemovedPartitionGroupStore> getRemovedPartitionGroups();

    /**
     * 从磁盘恢复partition group，系统启动时调用
     */
    void restorePartitionGroup(String topic, int partitionGroup) throws Exception;

    /**
     * 创建PartitionGroup。仅当topic创建或者向topic中添加partitionGroup的时候调用，需要提供节点信息。
     * 如果磁盘上数据目录不存在，自动创建数据目录；如果磁盘上存在数据目录，自动逻辑删除这些目录，然后创建新的数据目录。
     * @param topic Topic
     * @param partitionGroup Partition group
     * @param partitions Partition
     */
    void createPartitionGroup(String topic, int partitionGroup, short[] partitions) throws Exception;

    /**
     * 获取{@link PartitionGroupStore} 实例
     * @param topic Topic
     * @param partitionGroup Partition group
     * @param writeQosLevel 写入Qos级别
     * @return {@link PartitionGroupStore} 实例，不存在时返回null。
     */
    PartitionGroupStore getStore(String topic, int partitionGroup, QosLevel writeQosLevel);

    /**
     * 获取{@link PartitionGroupStore} 实例，使用默认的Qos级别
     * @see QosLevel#REPLICATION。
     * @see #getStore(java.lang.String, int, QosLevel)
     * @param topic Topic
     * @param partitionGroup Partition group
     * @return {@link PartitionGroupStore} 实例，不存在时返回null。
     */
    PartitionGroupStore getStore(String topic, int partitionGroup);

    /**
     * 获取指定 {@code topic} 下的全部 {@link PartitionGroupStore} 实例列表。
     * @param topic Topic
     * @return {@code topic} 下的全部 {@link PartitionGroupStore} 实例列表，{@code topic} 不存在时返回空List。
     */
    List<PartitionGroupStore> getStore(String topic);

    /**
     * 变更Partition group中的Partition
     * @throws NoSuchPartitionGroupException PartitionGroup不存在时抛出此异常
     * @throws IOException 创建/删除Partition时读写文件异常时抛出
     * @param topic Topic
     * @param partitionGroup Partition group
     * @param partitions 变更后的Partition数组
     */
    void rePartition(String topic, int partitionGroup, Short[] partitions) throws IOException;

    /**
     * 获取{@link ReplicableStore} 实例
     * @see QosLevel#REPLICATION。
     * @see #getStore(java.lang.String, int, QosLevel)
     * @param topic Topic
     * @param partitionGroup Partition group
     * @return {@link ReplicableStore} 实例，不存在时返回null。
     */

    ReplicableStore getReplicableStore(String topic, int partitionGroup);

    /**
     * 获取管理接口 {@link StoreManagementService} 实例
     * @return {@link StoreManagementService} 实例
     */
    StoreManagementService getManageService();

    /**
     * 获取内存监控信息。
     * @return 内存监控对象。
     */
    BufferPoolMonitorInfo monitorInfo();

    /**
     * 获取存储节点
     * @return
     */
    StoreNodes getNodes(String topic, int partitionGroup);

    /**
     * 添加监听器
     * @param listener
     */
    void addListener(EventListener<StoreEvent> listener);

    /**
     * 移除监听器
     * @param listener
     */
    void removeListener(EventListener<StoreEvent> listener);
}