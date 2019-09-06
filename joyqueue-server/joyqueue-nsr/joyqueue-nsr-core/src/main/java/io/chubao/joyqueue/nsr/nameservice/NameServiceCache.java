package io.chubao.joyqueue.nsr.nameservice;

import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.util.DCWrapper;
import io.chubao.joyqueue.toolkit.time.SystemClock;

import java.util.List;
import java.util.Map;

public class NameServiceCache {

    private Map<Integer /** brokerId **/, Broker> brokerMap;
    private List<Broker> allBrokers;
    private Map<TopicName, TopicConfig> topicConfigMap;
    private List<TopicConfig> allTopicConfigs;
    private List<String> allTopicCodes;
    private Map<Integer /** brokerId **/, Map<TopicName, TopicConfig>> topicConfigBrokerMap;
    private Map<TopicName /** topic **/, Map<String /** app **/, Producer>> producerTopicMap;
    private Map<String /** app **/, Map<TopicName /** topic **/, Producer>> producerAppMap;
    private List<Producer> allProducers;
    private Map<String /** app **/, Map<TopicName /** topic **/, Consumer>> consumerAppMap;
    private Map<TopicName /** topic **/, Map<String /** app **/, Consumer>> consumerTopicMap;
    private List<Consumer> allConsumers;
    private List<Config> allConfigs;
    private Map<String /** key **/, Config> configKeyMap;
    private Map<String /** app **/, List<AppToken>> allAppTokenMap;
    private List<DCWrapper> allDataCenters;
    private Map<String /** code **/, DCWrapper> dataCenterCodeMap;

    private volatile long lastTimestamp;

    public Map<Integer, Broker> getBrokerMap() {
        return brokerMap;
    }

    public void setBrokerMap(Map<Integer, Broker> brokerMap) {
        this.brokerMap = brokerMap;
    }

    public List<Broker> getAllBrokers() {
        return allBrokers;
    }

    public void setAllBrokers(List<Broker> allBrokers) {
        this.allBrokers = allBrokers;
    }

    public Map<TopicName, TopicConfig> getTopicConfigMap() {
        return topicConfigMap;
    }

    public void setTopicConfigMap(Map<TopicName, TopicConfig> topicConfigMap) {
        this.topicConfigMap = topicConfigMap;
    }

    public List<TopicConfig> getAllTopicConfigs() {
        return allTopicConfigs;
    }

    public void setAllTopicConfigs(List<TopicConfig> allTopicConfigs) {
        this.allTopicConfigs = allTopicConfigs;
    }

    public List<String> getAllTopicCodes() {
        return allTopicCodes;
    }

    public void setAllTopicCodes(List<String> allTopicCodes) {
        this.allTopicCodes = allTopicCodes;
    }

    public Map<Integer, Map<TopicName, TopicConfig>> getTopicConfigBrokerMap() {
        return topicConfigBrokerMap;
    }

    public void setTopicConfigBrokerMap(Map<Integer, Map<TopicName, TopicConfig>> topicConfigBrokerMap) {
        this.topicConfigBrokerMap = topicConfigBrokerMap;
    }

    public Map<TopicName, Map<String, Producer>> getProducerTopicMap() {
        return producerTopicMap;
    }

    public void setProducerTopicMap(Map<TopicName, Map<String, Producer>> producerTopicMap) {
        this.producerTopicMap = producerTopicMap;
    }

    public void setAllProducers(List<Producer> allProducers) {
        this.allProducers = allProducers;
    }

    public List<Producer> getAllProducers() {
        return allProducers;
    }

    public Map<String, Map<TopicName, Producer>> getProducerAppMap() {
        return producerAppMap;
    }

    public void setProducerAppMap(Map<String, Map<TopicName, Producer>> producerAppMap) {
        this.producerAppMap = producerAppMap;
    }

    public Map<String, Map<TopicName, Consumer>> getConsumerAppMap() {
        return consumerAppMap;
    }

    public void setConsumerAppMap(Map<String, Map<TopicName, Consumer>> consumerAppMap) {
        this.consumerAppMap = consumerAppMap;
    }

    public Map<TopicName, Map<String, Consumer>> getConsumerTopicMap() {
        return consumerTopicMap;
    }

    public void setConsumerTopicMap(Map<TopicName, Map<String, Consumer>> consumerTopicMap) {
        this.consumerTopicMap = consumerTopicMap;
    }

    public List<Consumer> getAllConsumers() {
        return allConsumers;
    }

    public void setAllConsumers(List<Consumer> allConsumers) {
        this.allConsumers = allConsumers;
    }

    public List<Config> getAllConfigs() {
        return allConfigs;
    }

    public void setAllConfigs(List<Config> allConfigs) {
        this.allConfigs = allConfigs;
    }

    public Map<String, Config> getConfigKeyMap() {
        return configKeyMap;
    }

    public void setConfigKeyMap(Map<String, Config> configKeyMap) {
        this.configKeyMap = configKeyMap;
    }

    public Map<String, List<AppToken>> getAllAppTokenMap() {
        return allAppTokenMap;
    }

    public void setAllAppTokenMap(Map<String, List<AppToken>> allAppTokenMap) {
        this.allAppTokenMap = allAppTokenMap;
    }

    public List<DCWrapper> getAllDataCenters() {
        return allDataCenters;
    }

    public void setAllDataCenters(List<DCWrapper> allDataCenters) {
        this.allDataCenters = allDataCenters;
    }

    public Map<String, DCWrapper> getDataCenterCodeMap() {
        return dataCenterCodeMap;
    }

    public void setDataCenterCodeMap(Map<String, DCWrapper> dataCenterCodeMap) {
        this.dataCenterCodeMap = dataCenterCodeMap;
    }

    public boolean isLatest(int interval) {
        return SystemClock.now() - lastTimestamp <= interval;
    }

    public void updateLastTimestamp() {
        this.lastTimestamp = SystemClock.now();
    }

    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }
}