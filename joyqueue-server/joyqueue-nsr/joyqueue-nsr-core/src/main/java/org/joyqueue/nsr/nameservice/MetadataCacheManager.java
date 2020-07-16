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
import com.google.common.collect.Sets;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.domain.AllMetadata;
import org.joyqueue.domain.AppToken;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.Config;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.DataCenter;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.Replica;
import org.joyqueue.domain.Subscription;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.config.NameServiceConfig;
import org.joyqueue.nsr.exception.NsrException;
import org.joyqueue.nsr.util.DCWrapper;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * NameServiceCacheManager
 * author: gaohaoxiang
 * date: 2019/8/30
 */
public class MetadataCacheManager extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(MetadataCacheManager.class);

    private NameServiceConfig config;

    private MetadataCacheDoubleCopy metadataCacheDoubleCopy;
    private volatile AllMetadataCache cache;
    private ReentrantLock lock = new ReentrantLock();
    private volatile long timestamp = 0;

    public MetadataCacheManager(NameServiceConfig config) {
        this.config = config;
    }

    @Override
    protected void validate() throws Exception {
        metadataCacheDoubleCopy = new MetadataCacheDoubleCopy(new File(config.getAllMetadataCacheFile()));
    }

    @Override
    protected void doStart() throws Exception {
        metadataCacheDoubleCopy.recover();
        this.cache = metadataCacheDoubleCopy.getCache();
    }

    public AllMetadataCache buildCache(AllMetadata allMetadata) {
        Map<Integer, Broker> brokerMap = Maps.newHashMap(allMetadata.getBrokers());
        Map<TopicName, TopicConfig> topicConfigMap = Maps.newHashMap(allMetadata.getTopics());
        List<Producer> allProducers = Lists.newLinkedList(allMetadata.getProducers());
        List<Consumer> allConsumers = Lists.newLinkedList(allMetadata.getConsumers());
        List<DataCenter> allDataCenters = Lists.newLinkedList(allMetadata.getDataCenters());
        List<Config> allConfigs = Lists.newLinkedList(allMetadata.getConfigs());
        List<AppToken> allAppTokens = Lists.newLinkedList(allMetadata.getAppTokens());

        List<Broker> allBrokers = Lists.newLinkedList();
        List<TopicConfig> allTopicConfigs = Lists.newLinkedList();
        List<String> allTopicCodes = Lists.newLinkedList();
        Map<Integer /** brokerId **/, Map<TopicName, TopicConfig>> topicConfigBrokerMap = Maps.newHashMapWithExpectedSize(brokerMap.size());

        Map<TopicName /** topic **/, Map<String /** app **/, Producer>> producerTopicMap = Maps.newHashMapWithExpectedSize(allTopicConfigs.size());
        Map<TopicName /** topic **/, Map<String /** app **/, Consumer>> consumerTopicMap = Maps.newHashMapWithExpectedSize(allTopicConfigs.size());
        Map<String /** app **/, Map<TopicName /** topic **/, Producer>> producerAppMap = Maps.newHashMapWithExpectedSize(allTopicConfigs.size());
        Map<String /** app **/, Map<TopicName /** topic **/, Consumer>> consumerAppMap = Maps.newHashMapWithExpectedSize(allTopicConfigs.size());

        Map<String /** app **/, List<AppToken>> allAppTokenMap = Maps.newHashMapWithExpectedSize(brokerMap.size());
        List<DCWrapper> allDataCenterWrappers = Lists.newLinkedList();
        Map<String /** id **/, Config> configKeyMap = Maps.newHashMapWithExpectedSize(allConfigs.size());
        Map<String /** code **/, DCWrapper> dataCenterWrapperCodeMap = Maps.newHashMapWithExpectedSize(allDataCenterWrappers.size());

        for (Map.Entry<TopicName, TopicConfig> topicEntry : topicConfigMap.entrySet()) {
            TopicName topicName = topicEntry.getKey();
            TopicConfig topicConfig = topicEntry.getValue();

            // 维护主题映射
            allTopicConfigs.add(topicConfig);
            allTopicCodes.add(topicName.getFullName());

            for (Map.Entry<Integer, PartitionGroup> partitionGroupEntry : topicConfig.getPartitionGroups().entrySet()) {
                PartitionGroup partitionGroup = partitionGroupEntry.getValue();
                Map<Integer, Broker> partitionGroupBrokerMap = Maps.newHashMap();
                partitionGroup.setBrokers(partitionGroupBrokerMap);

                for (Integer replica : partitionGroup.getReplicas()) {
                    // 维护partitionGroup Broker
                    Broker partitionGroupBroker = brokerMap.get(replica);
                    if (partitionGroupBroker != null) {
                        partitionGroupBrokerMap.put(replica, partitionGroupBroker);
                    }

                    // 维护broker映射
                    Map<TopicName, TopicConfig> brokerTopicConfigMap = topicConfigBrokerMap.get(replica);
                    if (brokerTopicConfigMap == null) {
                        brokerTopicConfigMap = Maps.newHashMap();
                        topicConfigBrokerMap.put(replica, brokerTopicConfigMap);
                    }
                    brokerTopicConfigMap.put(topicName, topicConfig);
                }
            }
        }

        for (Map.Entry<Integer, Broker> brokerEntry : brokerMap.entrySet()) {
            Broker broker = brokerEntry.getValue();

            allBrokers.add(broker);
        }

        for (Producer producer : allProducers) {

            // topic/app
            Map<String, Producer> appProducerMap = producerTopicMap.get(producer.getTopic());
            if (appProducerMap == null) {
                appProducerMap = Maps.newHashMap();
                producerTopicMap.put(producer.getTopic(), appProducerMap);
            }
            appProducerMap.put(producer.getApp(), producer);

            // app/topic
            Map<TopicName, Producer> topicProducerMap = producerAppMap.get(producer.getApp());
            if (topicProducerMap == null) {
                topicProducerMap = Maps.newHashMap();
                producerAppMap.put(producer.getApp(), topicProducerMap);
            }
            topicProducerMap.put(producer.getTopic(), producer);
        }

        for (Consumer consumer : allConsumers) {

            // topic/app
            Map<String, Consumer> appConsumerMap = consumerTopicMap.get(consumer.getTopic());
            if (appConsumerMap == null) {
                appConsumerMap = Maps.newHashMap();
                consumerTopicMap.put(consumer.getTopic(), appConsumerMap);
            }
            appConsumerMap.put(consumer.getApp(), consumer);

            // app/topic
            Map<TopicName, Consumer> topicConsumerMap = consumerAppMap.get(consumer.getApp());
            if (topicConsumerMap == null) {
                topicConsumerMap = Maps.newHashMap();
                consumerAppMap.put(consumer.getApp(), topicConsumerMap);
            }
            topicConsumerMap.put(consumer.getTopic(), consumer);
        }

        for (AppToken appToken : allAppTokens) {
            List<AppToken> appTokens = allAppTokenMap.get(appToken.getApp());
            if (appTokens == null) {
                appTokens = Lists.newLinkedList();
                allAppTokenMap.put(appToken.getApp(), appTokens);
            }
            appTokens.add(appToken);
        }

        for (Config config : allConfigs) {
            configKeyMap.put(config.getId(), config);
        }

        for (DataCenter dataCenter : allDataCenters) {
            DCWrapper dcWrapper = new DCWrapper(dataCenter);
            allDataCenterWrappers.add(dcWrapper);
            dataCenterWrapperCodeMap.put(dataCenter.getCode(), dcWrapper);
        }

        AllMetadataCache cache = new AllMetadataCache();
        cache.setAllBrokers(allBrokers);
        cache.setBrokerMap(brokerMap);
        cache.setTopicConfigMap(topicConfigMap);
        cache.setAllTopicConfigs(allTopicConfigs);
        cache.setAllTopicCodes(allTopicCodes);
        cache.setTopicConfigBrokerMap(topicConfigBrokerMap);
        cache.setProducerTopicMap(producerTopicMap);
        cache.setProducerAppMap(producerAppMap);
        cache.setAllProducers(allProducers);
        cache.setConsumerTopicMap(consumerTopicMap);
        cache.setConsumerAppMap(consumerAppMap);
        cache.setAllConsumers(allConsumers);
        cache.setAllConfigs(allConfigs);
        cache.setConfigKeyMap(configKeyMap);
        cache.setAllAppTokenMap(allAppTokenMap);
        cache.setAllDataCenters(allDataCenterWrappers);
        cache.setDataCenterCodeMap(dataCenterWrapperCodeMap);
        return cache;
    }

    public void fillCache(AllMetadataCache cache) {
        this.cache = cache;
    }

    public void flushCache() {
        metadataCacheDoubleCopy.flush(cache);
    }

    public Broker getBroker(int brokerId) {
        checkCacheStatus();
        return cache.getBrokerMap().get(brokerId);
    }

    public List<Broker> getAllBrokers() {
        checkCacheStatus();
        return cache.getAllBrokers();
    }

    public TopicConfig getTopicConfig(TopicName topic) {
        checkCacheStatus();
        return cache.getTopicConfigMap().get(topic);
    }

    public Set<String> getAllTopicCodes() {
        checkCacheStatus();
        return Sets.newHashSet(cache.getAllTopicCodes());
    }

    public Set<String> getTopics(String app, Subscription.Type subscription) {
        checkCacheStatus();
        if (Subscription.Type.PRODUCTION.equals(subscription)) {
            Map<TopicName, Producer> producerMap = cache.getProducerAppMap().get(app);
            if (producerMap == null) {
                return Collections.emptySet();
            }
            Set<String> result = Sets.newHashSet();
            for (Map.Entry<TopicName, Producer> entry : producerMap.entrySet()) {
                result.add(entry.getKey().getFullName());
            }
            return result;
        } else if (Subscription.Type.CONSUMPTION.equals(subscription)) {
            Map<TopicName, Consumer> consumerMap = cache.getConsumerAppMap().get(app);
            if (consumerMap == null) {
                return Collections.emptySet();
            }
            Set<String> result = Sets.newHashSet();
            for (Map.Entry<TopicName, Consumer> entry : consumerMap.entrySet()) {
                result.add(entry.getKey().getFullName());
            }
            return result;
        } else {
            throw new UnsupportedOperationException(subscription.name());
        }
    }

    public Map<TopicName, TopicConfig> getTopicConfigByBroker(Integer brokerId) {
        checkCacheStatus();
        return ObjectUtils.defaultIfNull(cache.getTopicConfigBrokerMap().get(brokerId), Collections.emptyMap());
    }

    public Map<TopicName, Producer> getProducerByApp(String app) {
        return cache.getProducerAppMap().get(app);
    }

    public Producer getProducerByTopicAndApp(TopicName topic, String app) {
        checkCacheStatus();
        Map<String, Producer> producerMap = cache.getProducerTopicMap().get(topic);
        if (producerMap == null) {
            return null;
        }
        return producerMap.get(app);
    }

    public Map<TopicName, Consumer> getConsumerByApp(String app) {
        return cache.getConsumerAppMap().get(app);
    }

    public Consumer getConsumerByTopicAndApp(TopicName topic, String app) {
        checkCacheStatus();
        Map<String, Consumer> consumerMap = cache.getConsumerTopicMap().get(topic);
        if (consumerMap == null) {
            return null;
        }
        return consumerMap.get(app);
    }

    public Map<TopicName, TopicConfig> getTopicConfigByApp(String subscribeApp, Subscription.Type subscribe) {
        checkCacheStatus();
        if (Subscription.Type.PRODUCTION.equals(subscribe)) {
            Map<TopicName, Producer> producerMap = cache.getProducerAppMap().get(subscribeApp);
            if (producerMap == null) {
                return Collections.emptyMap();
            }
            Map<TopicName, TopicConfig> result = Maps.newHashMapWithExpectedSize(producerMap.size());
            for (Map.Entry<TopicName, Producer> entry : producerMap.entrySet()) {
                TopicName topicName = entry.getKey();
                TopicConfig topicConfig = cache.getTopicConfigMap().get(topicName);
                if (topicConfig != null) {
                    result.put(topicName, topicConfig);
                }
            }
            return result;
        } else if (Subscription.Type.CONSUMPTION.equals(subscribe)) {
            Map<TopicName, Consumer> consumerMap = cache.getConsumerAppMap().get(subscribeApp);
            if (consumerMap == null) {
                return Collections.emptyMap();
            }
            Map<TopicName, TopicConfig> result = Maps.newHashMapWithExpectedSize(consumerMap.size());
            for (Map.Entry<TopicName, Consumer> entry : consumerMap.entrySet()) {
                TopicName topicName = entry.getKey();
                TopicConfig topicConfig = cache.getTopicConfigMap().get(topicName);
                if (topicConfig != null) {
                    result.put(topicName, topicConfig);
                }
            }
            return result;
        } else {
            throw new UnsupportedOperationException(subscribe.name());
        }
    }

    public DataCenter getDataCenter(String ip) {
        checkCacheStatus();
        for (DCWrapper dataCenter : cache.getAllDataCenters()) {
            if (dataCenter.match(ip)) {
                return dataCenter.getDataCenter();
            }
        }
        return null;
    }

    public String getConfig(String group, String key) {
        checkCacheStatus();
        List<Config> configs = cache.getAllConfigs();
        for (Config config : configs) {
            if (StringUtils.equals(config.getGroup(), group) && StringUtils.equals(config.getKey(), key)) {
                return config.getValue();
            }
        }
        return null;
    }

    public List<Config> getAllConfigs() {
        checkCacheStatus();
        return cache.getAllConfigs();
    }

    public List<Broker> getBrokerByRetryType(String retryType) {
        checkCacheStatus();
        List<Broker> result = Lists.newLinkedList();
        for (Broker broker : cache.getAllBrokers()) {
            if (StringUtils.equals(broker.getRetryType(), retryType)) {
                result.add(broker);
            }
        }
        return result;
    }

    public List<Consumer> getConsumerByTopic(TopicName topic) {
        checkCacheStatus();
        Map<String, Consumer> consumerMap = cache.getConsumerTopicMap().get(topic);
        if (consumerMap == null) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(consumerMap.values());
    }

    public List<Producer> getProducerByTopic(TopicName topic) {
        checkCacheStatus();
        Map<String, Producer> producerMap = cache.getProducerTopicMap().get(topic);
        if (producerMap == null) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(producerMap.values());
    }

    public List<Replica> getReplicaByBroker(Integer brokerId) {
        checkCacheStatus();
        Map<TopicName, TopicConfig> brokerTopicConfigMap = cache.getTopicConfigBrokerMap().get(brokerId);
        if (brokerTopicConfigMap == null) {
            return Collections.emptyList();
        }
        List<Replica> result = Lists.newLinkedList();
        for (Map.Entry<TopicName, TopicConfig> entry : brokerTopicConfigMap.entrySet()) {
            TopicConfig topicConfig = entry.getValue();
            for (Map.Entry<Integer, PartitionGroup> partitionGroupEntry : topicConfig.getPartitionGroups().entrySet()) {
                PartitionGroup partitionGroup = partitionGroupEntry.getValue();
                if (partitionGroup.getReplicas().contains(brokerId)) {
                    result.add(new Replica(String.valueOf(brokerId), topicConfig.getName(), partitionGroup.getGroup(), brokerId));
                }
            }
        }
        return result;
    }

    public AppToken getAppToken(String app, String token) {
        checkCacheStatus();
        List<AppToken> appTokens = cache.getAllAppTokenMap().get(app);
        if (appTokens == null) {
            return null;
        }
        for (AppToken appToken : appTokens) {
            if (StringUtils.equals(appToken.getToken(), token)) {
                return appToken;
            }
        }
        return null;
    }

    protected void checkCacheStatus() {
        if (cache != null) {
            return;
        }
        throw new NsrException();
    }

    public AllMetadataCache getCache() {
        return cache;
    }

    public boolean tryLock() {
        return lock.tryLock();
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public boolean isLocked() {
        return lock.isLocked();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void updateTimestamp() {
        timestamp = SystemClock.now();
    }
}