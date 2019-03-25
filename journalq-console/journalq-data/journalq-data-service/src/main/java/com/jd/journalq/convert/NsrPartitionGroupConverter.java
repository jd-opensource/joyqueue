package com.jd.journalq.convert;

import com.jd.journalq.common.domain.PartitionGroup;
import com.jd.journalq.model.domain.Namespace;
import com.jd.journalq.model.domain.PartitionGroupReplica;
import com.jd.journalq.model.domain.Topic;
import com.jd.journalq.model.domain.TopicPartitionGroup;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by wangxiaofei1 on 2019/1/3.
 */
public class NsrPartitionGroupConverter extends Converter<TopicPartitionGroup,PartitionGroup> {
    @Override
    protected PartitionGroup forward(TopicPartitionGroup topicPartitionGroup) {
        PartitionGroup partitionGroup = new PartitionGroup();
        partitionGroup.setTopic(CodeConverter.convertTopic(topicPartitionGroup.getNamespace(),topicPartitionGroup.getTopic()));
        partitionGroup.setElectType(PartitionGroup.ElectType.valueOf(topicPartitionGroup.getElectType()));
        partitionGroup.setGroup(topicPartitionGroup.getGroupNo());
        if (topicPartitionGroup.getPartitionSet() != null && topicPartitionGroup.getPartitionSet().size() > 0) {
            Set<Integer> partitionSet = topicPartitionGroup.getPartitionSet();
            partitionGroup.setPartitions(partitionSet.stream().map( partition-> Short.valueOf(String.valueOf(partition))).collect(Collectors.toSet()));
        }
        if (topicPartitionGroup.getReplicaGroups() != null) {
            int leader = -1;
            Set<Integer> learners = new TreeSet<>();
            Set<Integer> replicaGroups = new TreeSet<>();
            for(PartitionGroupReplica replica : topicPartitionGroup.getReplicaGroups()){
                replicaGroups.add(replica.getBrokerId());
                if(replica.getRole()==PartitionGroupReplica.ROLE_LEARNER)learners.add(replica.getBrokerId());
                else if(replica.getRole()==PartitionGroupReplica.ROLE_MASTER)leader = replica.getBrokerId();
            }
            partitionGroup.setLearners(learners);
            partitionGroup.setLeader(leader);
        }
        partitionGroup.setRecLeader(topicPartitionGroup.getRecLeader());
        return partitionGroup;
    }

    @Override
    protected TopicPartitionGroup backward(PartitionGroup partitionGroup) {
        TopicPartitionGroup topicPartitionGroup = new TopicPartitionGroup();
        topicPartitionGroup.setId(partitionGroup.getId());
        topicPartitionGroup.setElectType(partitionGroup.getElectType().type());
        topicPartitionGroup.setGroupNo(partitionGroup.getGroup());
        topicPartitionGroup.setIsr(partitionGroup.getIsrs());
        topicPartitionGroup.setLeader(partitionGroup.getLeader());
        if (partitionGroup.getPartitions() != null) {
            topicPartitionGroup.setPartitions(Arrays.toString(partitionGroup.getPartitions().toArray()));
        }
        topicPartitionGroup.setRecLeader(partitionGroup.getRecLeader());
        topicPartitionGroup.setLearners(partitionGroup.getLearners());
        topicPartitionGroup.setTerm(partitionGroup.getTerm());
        topicPartitionGroup.setTopic(new Topic(partitionGroup.getTopic().getCode()));
        topicPartitionGroup.setNamespace(new Namespace(partitionGroup.getTopic().getNamespace()));
        return topicPartitionGroup;
    }
}
