package io.chubao.joyqueue.domain;

import java.util.List;
import java.util.Map;

/**
 * AllMetadata
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class AllMetadata {

    private Map<TopicName, TopicConfig> topics;
    private Map<Integer, Broker> brokers;
    private List<Producer> producers;
    private List<Consumer> consumers;
    private List<DataCenter> dataCenters;
    private List<Config> configs;
    private List<AppToken> appTokens;

    public Map<TopicName, TopicConfig> getTopics() {
        return topics;
    }

    public void setTopics(Map<TopicName, TopicConfig> topics) {
        this.topics = topics;
    }

    public Map<Integer, Broker> getBrokers() {
        return brokers;
    }

    public void setBrokers(Map<Integer, Broker> brokers) {
        this.brokers = brokers;
    }

    public List<Producer> getProducers() {
        return producers;
    }

    public void setProducers(List<Producer> producers) {
        this.producers = producers;
    }

    public List<Consumer> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<Consumer> consumers) {
        this.consumers = consumers;
    }

    public List<DataCenter> getDataCenters() {
        return dataCenters;
    }

    public void setDataCenters(List<DataCenter> dataCenters) {
        this.dataCenters = dataCenters;
    }

    public List<Config> getConfigs() {
        return configs;
    }

    public void setConfigs(List<Config> configs) {
        this.configs = configs;
    }

    public void setAppTokens(List<AppToken> appTokens) {
        this.appTokens = appTokens;
    }

    public List<AppToken> getAppTokens() {
        return appTokens;
    }
}