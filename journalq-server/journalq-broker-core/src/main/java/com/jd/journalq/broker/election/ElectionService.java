package com.jd.journalq.broker.election;

import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.toolkit.concurrent.EventListener;

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
     * Sync election meta data from name service
     */
    void syncElectionMetadataFromNameService();
}
