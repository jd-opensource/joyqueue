package com.jd.joyqueue.broker.jmq2.entity;

import com.jd.joyqueue.broker.jmq2.command.BrokerCluster;

import java.util.List;
import java.util.Map;

public class GetClusterEntity {
    private Map<String, TopicEntity> topicMapper;
    private List<BrokerCluster> brokerClusters;

    public GetClusterEntity() {

    }

    public GetClusterEntity(Map<String, TopicEntity> topicMapper, List<BrokerCluster> brokerClusters) {
        this.topicMapper = topicMapper;
        this.brokerClusters = brokerClusters;
    }

    public void setTopicMapper(Map<String, TopicEntity> topicMapper) {
        this.topicMapper = topicMapper;
    }

    public void setBrokerClusters(List<BrokerCluster> brokerClusters) {
        this.brokerClusters = brokerClusters;
    }

    public Map<String, TopicEntity> getTopicMapper() {
        return topicMapper;
    }

    public List<BrokerCluster> getBrokerClusters() {
        return brokerClusters;
    }
}