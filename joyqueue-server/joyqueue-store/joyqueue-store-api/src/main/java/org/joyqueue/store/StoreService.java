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

import org.joyqueue.monitor.BufferPoolMonitorInfo;
import org.joyqueue.store.transaction.TransactionStore;
import java.io.IOException;
import java.util.Collection;
import java.util.List;


/**
 * Broker 存储元数据服务
 */
public interface StoreService {

    /**
     * 获取事务消息使用的{@link TransactionStore}
     * @return 如果 {@link TransactionStore}存在则直接返回；
     * 如果Topic存在，{@link TransactionStore}目录文件不存在则自动创建 {@link TransactionStore}；
     * 如果Topic不存在，返回null。
     */
    TransactionStore getTransactionStore(String topic, short partition);

    /**
     * 获取全部{@link TransactionStore}
     * @return {@link TransactionStore}列表
     */
    List<TransactionStore> getAllTransactionStores();

    /**
     * 删除Partition group。
     * @param topic Topic
     * @param partitionGroup Partition group
     */
    void removePartitionGroup(String topic, int partitionGroup);

    /**
     * 从磁盘恢复或新建 partition group，系统启动时调用
     * @param topic  topic of the partition group
     * @param partitionGroup  partition group id
     * @param partitions  partitions included in the partition group
     * @param brokers   replicas broker of the partition group
     * @param thisBrokerId local broker
     */
    PartitionGroupStore restoreOrCreatePartitionGroup(String topic, int partitionGroup, short[] partitions, List<Integer> brokers, int thisBrokerId);

    /**
     * 创建PartitionGroup。仅当topic创建或者向topic中添加partitionGroup的时候调用，需要提供节点信息。
     * 如果磁盘上数据目录不存在，自动创建数据目录；如果磁盘上存在数据目录，自动逻辑删除这些目录，然后创建新的数据目录。
     * @param topic Topic
     * @param partitionGroup Partition group
     * @param partitions Partition
     * @param brokers 所有副本（包含本节点）的BrokerId
     * @param thisBrokerId 本节点的BrokerId
     * @return See {@link PartitionGroupStore}
     */
    PartitionGroupStore createPartitionGroup(String topic, int partitionGroup, short[] partitions, List<Integer> brokers, int thisBrokerId);

    /**
     * 停止Partition group
     * @param topic Topic
     * @param partitionGroup Partition group
     */
    void stopPartitionGroup(String topic, int partitionGroup);
    /**
     * 获取{@link PartitionGroupStore} 实例
     * @param topic Topic
     * @param partitionGroup Partition group
     * @return {@link PartitionGroupStore} 实例，不存在时返回null。
     */
    PartitionGroupStore getStore(String topic, int partitionGroup);

    /**
     * 获取全部的Store
     * @return 所有store的集合
     */
    Collection<PartitionGroupStore> getAllStores();
    /**
     * 获取指定 {@code topic} 下的全部 {@link PartitionGroupStore} 实例列表。
     * @param topic Topic
     * @return {@code topic} 下的全部 {@link PartitionGroupStore} 实例列表，{@code topic} 不存在时返回空List。
     */
    List<PartitionGroupStore> getStore(String topic);

    /**
     * 如果当前节点是Leader，变更Partition group中的Partition
     * @throws NoSuchPartitionGroupException PartitionGroup不存在时抛出此异常
     * @throws IOException 创建/删除Partition时读写文件异常时抛出
     * @param topic Topic
     * @param partitionGroup Partition group
     * @param partitions 变更后的Partition数组
     */
    void maybeRePartition(String topic, int partitionGroup, Collection<Short> partitions) throws IOException;

    /**
     * 如果当前节点是Leader，执行副本分布节点变更
     * @param newReplicaBrokerIds 变更后的新配置
     */
    void maybeUpdateReplicas(String topic, int partitionGroup, Collection<Integer> newReplicaBrokerIds);

    /**
     * 获取管理接口 {@link StoreManagementService} 实例
     * @return {@link StoreManagementService} 实例
     */
    StoreManagementService getManageService();

    /**
     * 获取内存监控
     * @return
     */
    BufferPoolMonitorInfo monitorInfo();

    List<TransactionStore> getTransactionStores(String topic);
}
