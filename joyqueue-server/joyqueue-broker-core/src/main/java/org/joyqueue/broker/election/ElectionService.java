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
package org.joyqueue.broker.election;

import org.joyqueue.domain.Broker;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicName;
import org.joyqueue.toolkit.concurrent.EventListener;

import java.util.List;
import java.util.Set;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/28
 */
public interface ElectionService {

    /**
     * This method is called when a partition group is created
     * @param electType  election type, fix or raft
     * @param topic topic
     * @param partitionGroup partition group id
     * @param allNodes  all brokers of the cluster
     * @param learners  brokers which don't take part in vote
     * @param localBroker  local broker id
     * @param leader  leader broker id of the cluster
     * @throws ElectionException exception
     */
    void onPartitionGroupCreate(PartitionGroup.ElectType electType, TopicName topic, int partitionGroup,
                                List<Broker> allNodes, Set<Integer> learners, int localBroker, int leader) throws ElectionException;

    /**
     * This method is called when a partition group is removed.
     * @param topic topic
     * @param partitionGroup partition group id
     */
    void onPartitionGroupRemove(TopicName topic, int partitionGroup);

    /**
     * This method is called when a node is removed from a partition group.
     * @param topic topic
     * @param partitionGroup partition group id
     * @param brokerId broker id
     * @throws ElectionException
     */
    void onNodeRemove(TopicName topic, int partitionGroup, int brokerId, int localBroker) throws ElectionException;

    /**
     * This method is called when a node is added to a partition group.
     * @param topic topic
     * @param partitionGroup partition group id
     * @param broker broker will be added
     * @throws ElectionException
     */
    void onNodeAdd(TopicName topic, int partitionGroup, PartitionGroup.ElectType electType, List<Broker> allNodes,
                   Set<Integer> learners, Broker broker, int localBroker, int leader) throws ElectionException;

    /**
     * This method is called when the election type of a partition group is changed.
     * For example, from raft to fix or from fix to raft.
     * @param topic topic
     * @param partitionGroup partition group id
     * @param electType election type, fix or raft
     * @param allNodes  all brokers after election type change
     * @param learners  brokers that don't take part in vote
     * @param localBroker local broker id
     * @param leaderId  leader node after election type change
     * @throws ElectionException
     */
    void onElectionTypeChange(TopicName topic, int partitionGroup, PartitionGroup.ElectType electType,
                              List<Broker> allNodes, Set<Integer> learners, int localBroker, int leaderId) throws ElectionException;

    /**
     * This method is called when the leader of a partition group change.
     * @param topic topic
     * @param partitionGroup partition group id
     * @param leaderId leader id
     * @throws ElectionException
     */
    void onLeaderChange(TopicName topic, int partitionGroup, int leaderId) throws Exception;

    /**
     * add a listener to the cluster
     * @param listener listener
     */
    void addListener(EventListener<ElectionEvent> listener);

    /**
     * remove a listener from the cluster
     * @param listener listener
     */
    void removeListener(EventListener<ElectionEvent> listener);

    /**
     * Get leader election by topic and partition group.
     * @param topic topic
     * @param partitionGroup partition group id
     * @return  leader election
     */
    LeaderElection getLeaderElection(TopicName topic, int partitionGroup);

    /**
     * Get all leader election
     * @return leader election
     */
    List<LeaderElection> getLeaderElections();

    /**
     * Sync election meta data from name service
     */
    void syncElectionMetadataFromNameService();

    /**
     * Describe election metadata of all topics
     * @return description of election metadata
     */
    String describe();

    /**
     * Describe election metadata of specified topic
     * @param topic topic to describe
     * @param partitionGroup partition group to describe
     * @return description of election metadata
     */
    String describe(String topic, int partitionGroup);

    /**
     * Update term of the metadata
     * @param topic topic
     * @param partitionGroup partition group
     * @param term new term
     */
    void updateTerm(String topic, int partitionGroup, int term);
}
