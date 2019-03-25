package com.jd.journalq.broker.coordinator.domain;

import com.jd.journalq.common.domain.Broker;
import com.jd.journalq.common.domain.TopicName;

import java.util.List;

/**
 * CoordinatorDetail
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/13
 */
public class CoordinatorDetail {

    private TopicName topic;
    private int partitionGroup;
    private Broker current;
    private List<Broker> replicas;

    public CoordinatorDetail() {

    }

    public CoordinatorDetail(TopicName topic, int partitionGroup, Broker current, List<Broker> replicas) {
        this.topic = topic;
        this.partitionGroup = partitionGroup;
        this.current = current;
        this.replicas = replicas;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public void setPartitionGroup(int partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public Broker getCurrent() {
        return current;
    }

    public void setCurrent(Broker current) {
        this.current = current;
    }

    public List<Broker> getReplicas() {
        return replicas;
    }

    public void setReplicas(List<Broker> replicas) {
        this.replicas = replicas;
    }
}