package org.joyqueue.broker.cluster.entry;

import org.joyqueue.domain.Broker;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicName;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ClusterPartitionGroup
 * author: gaohaoxiang
 * date: 2020/3/31
 */
public class ClusterPartitionGroup extends PartitionGroup {

    private boolean rewrite = false;
    private PartitionGroup delegate;

    public ClusterPartitionGroup() {
        this(new PartitionGroup());
    }

    public ClusterPartitionGroup(PartitionGroup partitionGroup) {
        this.delegate = partitionGroup;
    }

    public void setRewrite(boolean rewrite) {
        this.rewrite = rewrite;
    }

    public boolean isRewrite() {
        return rewrite;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public Broker getLeaderBroker() {
        return delegate.getLeaderBroker();
    }

    @Override
    public TopicName getTopic() {
        return delegate.getTopic();
    }

    @Override
    public void setTopic(TopicName topic) {
        delegate.setTopic(topic);
    }

    @Override
    public Set<Integer> getIsrs() {
        return delegate.getIsrs();
    }

    @Override
    public void setIsrs(Set<Integer> isrs) {
        delegate.setIsrs(isrs);
    }

    @Override
    public Integer getTerm() {
        return delegate.getTerm();
    }

    @Override
    public void setTerm(Integer term) {
        delegate.setTerm(term);
    }

    @Override
    public Integer getRecLeader() {
        return delegate.getRecLeader();
    }

    @Override
    public void setRecLeader(Integer recLeader) {
        delegate.setRecLeader(recLeader);
    }

    @Override
    public Integer getLeader() {
        return delegate.getLeader();
    }

    @Override
    public void setLeader(Integer leader) {
        delegate.setLeader(leader);
    }

    @Override
    public Set<Short> getPartitions() {
        return delegate.getPartitions();
    }

    @Override
    public void setPartitions(Set<Short> partitions) {
        delegate.setPartitions(partitions);
    }

    @Override
    public Set<Integer> getReplicas() {
        return delegate.getReplicas();
    }

    @Override
    public void setReplicas(Set<Integer> replicas) {
        delegate.setReplicas(replicas);
    }

    @Override
    public int getGroup() {
        return delegate.getGroup();
    }

    @Override
    public void setGroup(int group) {
        delegate.setGroup(group);
    }

    @Override
    public Map<Integer, Broker> getBrokers() {
        return delegate.getBrokers();
    }

    @Override
    public ElectType getElectType() {
        return delegate.getElectType();
    }

    @Override
    public void setElectType(ElectType electType) {
        delegate.setElectType(electType);
    }

    @Override
    public void setBrokers(Map<Integer, Broker> brokers) {
        delegate.setBrokers(brokers);
    }

    @Override
    public Set<Integer> getLearners() {
        return delegate.getLearners();
    }

    @Override
    public void setLearners(Set<Integer> learners) {
        delegate.setLearners(learners);
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public PartitionGroup clone() {
        return delegate.clone();
    }

    @Override
    public List<Integer> getOutSyncReplicas() {
        return delegate.getOutSyncReplicas();
    }

    @Override
    public void setOutSyncReplicas(List<Integer> outSyncReplicas) {
        delegate.setOutSyncReplicas(outSyncReplicas);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}