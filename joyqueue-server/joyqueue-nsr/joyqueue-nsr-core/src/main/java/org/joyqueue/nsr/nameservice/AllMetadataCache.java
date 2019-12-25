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
package org.joyqueue.nsr.nameservice;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.domain.AppToken;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.Config;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.util.DCWrapper;

import java.util.List;
import java.util.Map;

public class AllMetadataCache {

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
    private Map<String /** id **/, Config> configKeyMap;
    private Map<String /** app **/, List<AppToken>> allAppTokenMap;
    private List<DCWrapper> allDataCenters;
    private Map<String /** code **/, DCWrapper> dataCenterCodeMap;

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

    public AllMetadataCache clone() {
        AllMetadataCache allMetadataCache = new AllMetadataCache();
        allMetadataCache.setBrokerMap(Maps.newHashMap(brokerMap));
        allMetadataCache.setAllBrokers(Lists.newArrayList(allBrokers));
        allMetadataCache.setTopicConfigMap(Maps.newHashMap(topicConfigMap));
        allMetadataCache.setAllTopicConfigs(Lists.newArrayList(allTopicConfigs));
        allMetadataCache.setAllTopicCodes(Lists.newArrayList(allTopicCodes));
        allMetadataCache.setTopicConfigBrokerMap(Maps.newHashMap(topicConfigBrokerMap));
        allMetadataCache.setProducerTopicMap(Maps.newHashMap(producerTopicMap));
        allMetadataCache.setProducerAppMap(Maps.newHashMap(producerAppMap));
        allMetadataCache.setAllProducers(Lists.newArrayList(allProducers));
        allMetadataCache.setConsumerAppMap(Maps.newHashMap(consumerAppMap));
        allMetadataCache.setConsumerTopicMap(Maps.newHashMap(consumerTopicMap));
        allMetadataCache.setAllConsumers(Lists.newArrayList(allConsumers));
        allMetadataCache.setAllConfigs(Lists.newArrayList(allConfigs));
        allMetadataCache.setConfigKeyMap(Maps.newHashMap(configKeyMap));
        allMetadataCache.setAllAppTokenMap(Maps.newHashMap(allAppTokenMap));
        allMetadataCache.setAllDataCenters(Lists.newArrayList(allDataCenters));
        allMetadataCache.setDataCenterCodeMap(Maps.newHashMap(dataCenterCodeMap));
        return allMetadataCache;
    }
}