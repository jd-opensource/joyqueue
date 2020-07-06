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


import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.model.domain.Broker;

import java.util.List;
import java.util.Map;

/**
 *
 *  提供topic 选主信息
 *
 **/
public interface LeaderService {

    /**
     *  根据topic id 查找,topic partition group 的主broker
     *  @return  partitionGroup list ,contain broker id
     *  or null, if not found
     **/
    List<PartitionGroup> findPartitionGroupLeaderBroker(String topic,String namespace);

    /***
     *
     *  根据topic id 查找,topic partition group 的主broker
     *  @param topic
     *  @return  Broker list or null ,if not found
     **/
    List<Broker> findLeaderBroker(String topic,String namespace);


    /***
     *
     *  根据topic id 查找,topic partition group 的主broker
     *  @param namespace  topic namespace id
     *  @param topic
     *  @param groupNo
     *  @return  a pair of partitionGroup,Broker list or null ,if not found
     **/
    Map.Entry<PartitionGroup, Broker> findPartitionGroupLeaderBrokerDetail(String namespace,String topic,int groupNo);



    /***
     *
     *  根据topic id 查找,topic partition 的主broker
     *  @param topic
     *  @param partition
     *  @return  a pair of partitionGroup,Broker list or null ,if not found
     **/
    Map.Entry<PartitionGroup, Broker> findPartitionLeaderBrokerDetail(String namespace,String topic,int partition);

    /**
     * @return  <PartitionGroup,Broker> kv list,parition group with leader broker detail
     *
     **/
    List<Map.Entry<PartitionGroup,Broker>> findPartitionGroupLeaderBrokerDetail(String topic,String namespace);


    /**
     * @return  <PartitionGroup,Broker> kv list,parition group with leader broker detail
     *
     **/
    Map<Short,Broker> findPartitionLeaderBrokerDetail(String topic,String namespace);




}
