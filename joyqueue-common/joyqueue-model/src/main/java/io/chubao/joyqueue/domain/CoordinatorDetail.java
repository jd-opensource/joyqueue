package io.chubao.joyqueue.domain;

import java.util.List;


/**
 *
 * 2019/01/23
 * @author  wangjin18
 *
 **/
public class CoordinatorDetail {
    private TopicName topic;
    private int partitionGroup;
    private Broker current;   // coordinator broker
    private List<Broker> replicas;

    public CoordinatorDetail(){

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
