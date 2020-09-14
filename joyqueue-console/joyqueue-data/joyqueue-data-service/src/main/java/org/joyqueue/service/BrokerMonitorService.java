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
package org.joyqueue.service;


import org.joyqueue.manage.PartitionGroupPosition;
import org.joyqueue.model.BrokerMetadata;
import org.joyqueue.model.domain.Broker;
import org.joyqueue.model.domain.BrokerClient;
import org.joyqueue.model.domain.BrokerMonitorRecord;
import org.joyqueue.model.domain.ConnectionMonitorInfoWithIp;
import org.joyqueue.model.domain.Subscribe;
import org.joyqueue.monitor.ArchiveMonitorInfo;

import java.util.List;

/**
 *
 * consider more than one partition group on same broker for a topic
 * and leader change among partition group broker cluster
 *
 * @author wangjin18
 *
 **/
public interface BrokerMonitorService {

    boolean removeBrokerMonitorConnections(Subscribe subscribe,Integer brokerid);

    /**
     * Find monitor info of a producer or consumer 汇总信息
     * @param subscribe consumer or producer subscribe info
     * @return a monitor record
     **/
    BrokerMonitorRecord find(Subscribe subscribe);


    /**
     * Find monitor info of a producer or consumer 汇总信息
     * @param subscribe consumer or producer subscribe info
     * @param active   if true , only contain leader partition group of this subscribe on the broker right now,which excluding
     *                 case of  the broker isn't partition group'leader but still remaining monitor info,such as enqueue and dequeun
     * @return a monitor record
     **/
    BrokerMonitorRecord find(Subscribe subscribe, boolean active);

    /**
     *
     * @param subscribe consumer or producer subscribe info
     * @return a monitor record list contain statistics info on each broker for topic and app
     * @see
     **/
    List<BrokerMonitorRecord> findMonitorOnBroker(Subscribe subscribe);


    /**
     * @return  topic and app 的生产或者消费客户端client 信息 和broker ip:port
     *
     **/
    List<BrokerClient> findClients(Subscribe subscribe);


    /**
     *
     * @return  每个partition的的监控消费或者生产监控 or null，sorted by partition
     * null 表示当前 该partition没有监控数据，一般是新建的生产或者消费关系，没有开始生产或者消费
     *
     **/
    List<BrokerMonitorRecord> findMonitorOnPartition(Subscribe subscribe);


    /**
     *
     * @param partitionGroup
     * @return  partition group 所在broker下每个 @code subscribe 所有partition的的监控消费或者生产监控 or null，sorted by partition
     * null 表示当前 该partition没有监控数据，一般是新建的生产或者消费关系，没有开始生产或者消费
     *
     *
     **/
    List<BrokerMonitorRecord> findMonitorOnPartition(Subscribe subscribe, int partitionGroup);

    /**
     *
     * @return  app 在某一topic所有 partition group的的监控消费或者生产监控 or null，sorted by partition group no;
     * null 表示当前 该partition group没有监控数据，一般是新建的生产或者消费关系，没有开始生产或者消费
     * 与findMonitorOnPartitionGroups 不同; 包含出队和积压监控信息汇总信息,for consumer or producer
     *
     **/
    List<BrokerMonitorRecord> findMonitorOnPartitionGroupsForTopicApp(Subscribe subscribe);

    /**
     *
     * @param partitionGroup target partitionGroup
     * @return  app 在某一topic指定partition group的全部partition监控消费或者生产监控 or null，sorted by partition;
     * null 表示当前 该partition没有监控数据，一般是新建的生产或者消费关系，没有开始生产或者消费;for consumer or producer
     * consider active partition
     **/
    List<BrokerMonitorRecord> findMonitorOnPartitionGroupDetailForTopicApp(Subscribe subscribe, int partitionGroup);

    /**
     * @return  每个partition group的的监控消费或者生产监控 or null，sorted by partition
     * null 表示当前 该partition没有监控数据，一般是新建的生产或者消费关系，没有开始生产或者消费
     *  仅是topic 的出入队监控,only for partition group 与app 无关
     *
     **/
    @Deprecated
    List<BrokerMonitorRecord> findMonitorOnPartitionGroups(Subscribe subscribe);


    /**
     * @return  获取topic, app 在某台broker 上的连接数,包括生产和消费连接数
     *
     **/
    List<ConnectionMonitorInfoWithIp> findConnectionOnBroker(Subscribe subscribe);

    /**
     * 查找Broker元数据
     * @param broker
     * @param topicFullName
     * @param group
     * @return
     */
    BrokerMetadata findBrokerMetadata(Broker broker, String topicFullName, int group);

    /**
     * 获取
     * @param namespace
     * @param topic
     * @param groupNo
     * @return
     */
    List<PartitionGroupPosition> findPartitionGroupMetric(String namespace, String topic, Integer groupNo);

   /**
    *
    * @param ip  broker ip
    * @param port management port
    * @return archive state on the broker or null when exception
    *
    **/
   ArchiveMonitorInfo findArchiveState(String ip, int port);



}
