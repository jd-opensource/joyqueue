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
import org.apache.commons.collections.MapUtils;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.Topic;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.event.NameServerEvent;
import org.joyqueue.nsr.config.NameServiceConfig;
import org.joyqueue.nsr.event.AddConsumerEvent;
import org.joyqueue.nsr.event.AddPartitionGroupEvent;
import org.joyqueue.nsr.event.AddProducerEvent;
import org.joyqueue.nsr.event.AddTopicEvent;
import org.joyqueue.nsr.event.RemoveConsumerEvent;
import org.joyqueue.nsr.event.RemovePartitionGroupEvent;
import org.joyqueue.nsr.event.RemoveProducerEvent;
import org.joyqueue.nsr.event.RemoveTopicEvent;
import org.joyqueue.nsr.event.UpdateBrokerEvent;
import org.joyqueue.nsr.event.UpdateConsumerEvent;
import org.joyqueue.nsr.event.UpdatePartitionGroupEvent;
import org.joyqueue.nsr.event.UpdateProducerEvent;
import org.joyqueue.nsr.event.UpdateTopicEvent;
import org.joyqueue.nsr.message.MessageListener;
import org.joyqueue.toolkit.concurrent.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MetadataCacheEventListener
 * author: gaohaoxiang
 * date: 2019/9/4
 */
public class MetadataCacheEventListener implements MessageListener<MetaEvent> {

    protected static final Logger logger = LoggerFactory.getLogger(MetadataCacheEventListener.class);

    private NameServiceConfig config;
    private EventBus<NameServerEvent> eventBus;
    private MetadataCacheManager metadataCacheManager;

    public MetadataCacheEventListener(NameServiceConfig config, EventBus<NameServerEvent> eventBus, MetadataCacheManager metadataCacheManager) {
        this.config = config;
        this.eventBus = eventBus;
        this.metadataCacheManager = metadataCacheManager;
    }

    @Override
    public void onEvent(MetaEvent event) {
        if (!metadataCacheManager.tryLock()) {
            metadataCacheManager.updateTimestamp();
            return;
        }
        try {
            AllMetadataCache newCache = metadataCacheManager.getCache().clone();
            boolean updateCache = doUpdateCache(event, newCache);
            if (!updateCache) {
                return;
            }
            doOnEvent(event);
            metadataCacheManager.fillCache(newCache);
            metadataCacheManager.updateTimestamp();
        } finally {
            metadataCacheManager.unlock();
        }
    }

    protected boolean doUpdateCache(MetaEvent event, AllMetadataCache cache) {
        switch (event.getEventType()) {
            case ADD_TOPIC: {
                AddTopicEvent addTopicEvent = (AddTopicEvent) event;
                TopicConfig oldTopicConfig = cache.getTopicConfigMap().get(addTopicEvent.getTopic().getName());
                if (oldTopicConfig != null) {
                    logger.warn("topic cache is exist, topic: {}", addTopicEvent.getTopic().getName());
                    break;
                }
                TopicConfig topicConfig = TopicConfig.toTopicConfig(addTopicEvent.getTopic(), addTopicEvent.getPartitionGroups());
                cache.getTopicConfigMap().put(topicConfig.getName(), topicConfig);
                cache.getAllTopicConfigs().add(topicConfig);
                cache.getAllTopicCodes().add(topicConfig.getName().getFullName());

                for (PartitionGroup partitionGroup : addTopicEvent.getPartitionGroups()) {
                    for (Integer replica : partitionGroup.getReplicas()) {
                        Map<TopicName, TopicConfig> topicConfigBrokerMap = cache.getTopicConfigBrokerMap().get(replica);
                        if (topicConfigBrokerMap == null) {
                            topicConfigBrokerMap = Maps.newHashMap();
                            cache.getTopicConfigBrokerMap().put(replica, topicConfigBrokerMap);
                        }
                        topicConfigBrokerMap.put(topicConfig.getName(), topicConfig);
                    }
                }

                break;
            }
            case UPDATE_TOPIC: {
                UpdateTopicEvent updateTopicEvent = (UpdateTopicEvent) event;
                Topic newTopic = updateTopicEvent.getNewTopic();
                TopicConfig oldTopicConfig = cache.getTopicConfigMap().get(newTopic.getName());
                if (oldTopicConfig == null) {
                    logger.warn("topic cache not exist, topic: {}", newTopic.getName());
                    break;
                }
                oldTopicConfig.setName(newTopic.getName());
                oldTopicConfig.setPartitions(newTopic.getPartitions());
                oldTopicConfig.setPriorityPartitions(newTopic.getPriorityPartitions());
                oldTopicConfig.setType(newTopic.getType());
                oldTopicConfig.setPolicy(newTopic.getPolicy());
                break;
            }
            case REMOVE_TOPIC: {
                RemoveTopicEvent removeTopicEvent = (RemoveTopicEvent) event;
                Topic topic = removeTopicEvent.getTopic();
                TopicConfig oldTopicConfig = cache.getTopicConfigMap().get(topic.getName());
                if (oldTopicConfig == null) {
                    logger.warn("topic cache not found, topic: {}", topic.getName());
                    break;
                }
                cache.getTopicConfigMap().remove(oldTopicConfig);
                cache.getAllTopicConfigs().remove(oldTopicConfig);
                cache.getAllTopicCodes().remove(oldTopicConfig);

                for (Map.Entry<Integer, PartitionGroup> partitionGroupEntry : oldTopicConfig.getPartitionGroups().entrySet()) {
                    for (Integer replica : partitionGroupEntry.getValue().getReplicas()) {
                        Map<TopicName, TopicConfig> brokerTopicConfigMap = cache.getTopicConfigBrokerMap().get(replica);
                        if (brokerTopicConfigMap != null) {
                            brokerTopicConfigMap.remove(topic.getName());
                        }
                    }
                }
                break;
            }
            case ADD_PARTITION_GROUP: {
                AddPartitionGroupEvent addPartitionGroupEvent = (AddPartitionGroupEvent) event;
                PartitionGroup partitionGroup = addPartitionGroupEvent.getPartitionGroup();
                TopicConfig oldTopicConfig = cache.getTopicConfigMap().get(partitionGroup.getTopic());
                if (oldTopicConfig == null) {
                    logger.warn("topic cache not exist, topic: {}", partitionGroup.getTopic());
                    break;
                }
                HashMap<Integer, PartitionGroup> topicPartitionGroups = Maps.newHashMap(oldTopicConfig.getPartitionGroups());
                topicPartitionGroups.put(partitionGroup.getGroup(), partitionGroup);
                oldTopicConfig.setPartitionGroups(topicPartitionGroups);

                for (Integer replica : partitionGroup.getReplicas()) {
                    Map<TopicName, TopicConfig> topicConfigBrokerMap = cache.getTopicConfigBrokerMap().get(replica);
                    if (topicConfigBrokerMap == null) {
                        topicConfigBrokerMap = Maps.newHashMap();
                        cache.getTopicConfigBrokerMap().put(replica, topicConfigBrokerMap);
                    }
                    topicConfigBrokerMap.put(oldTopicConfig.getName(), oldTopicConfig);
                }

                break;
            }
            case UPDATE_PARTITION_GROUP: {
                UpdatePartitionGroupEvent updatePartitionGroupEvent = (UpdatePartitionGroupEvent) event;
                TopicConfig oldTopicConfig = cache.getTopicConfigMap().get(updatePartitionGroupEvent.getTopic());
                if (oldTopicConfig == null) {
                    logger.warn("topic cache not exist, topic: {}", updatePartitionGroupEvent.getTopic());
                    break;
                }

                PartitionGroup oldPartitionGroup = updatePartitionGroupEvent.getOldPartitionGroup();

                if (MapUtils.isEmpty(oldTopicConfig.getPartitionGroups()) ||
                        !oldPartitionGroup.equals(oldTopicConfig.getPartitionGroups().get(oldPartitionGroup.getGroup()))) {
                    return false;
                }

                PartitionGroup newPartitionGroup = updatePartitionGroupEvent.getNewPartitionGroup();
                List<Integer> removedReplica = Lists.newLinkedList();

                for (Integer oldReplica : oldPartitionGroup.getReplicas()) {
                    if (!newPartitionGroup.getReplicas().contains(oldReplica)) {
                        removedReplica.add(oldReplica);
                    }
                }

                for (Integer newReplica : newPartitionGroup.getReplicas()) {
                    Map<TopicName, TopicConfig> brokerTopicConfigMap = cache.getTopicConfigBrokerMap().get(newReplica);
                    if (brokerTopicConfigMap == null) {
                        brokerTopicConfigMap = Maps.newHashMap();
                        cache.getTopicConfigBrokerMap().put(newReplica, brokerTopicConfigMap);
                    }
                    brokerTopicConfigMap.put(oldTopicConfig.getName(), oldTopicConfig);
                }

                for (Integer replica : removedReplica) {
                    Map<TopicName, TopicConfig> brokerTopicConfigMap = cache.getTopicConfigBrokerMap().get(replica);
                    if (brokerTopicConfigMap == null) {
                        continue;
                    }
                    boolean isMatch = false;
                    for (Map.Entry<Integer, PartitionGroup> entry : oldTopicConfig.getPartitionGroups().entrySet()) {
                        PartitionGroup partitionGroup = entry.getValue();
                        if (partitionGroup.getGroup() != newPartitionGroup.getGroup() &&
                                partitionGroup.getReplicas().contains(replica)) {
                            isMatch = true;
                            break;
                        }
                    }
                    if (!isMatch) {
                        brokerTopicConfigMap.remove(oldTopicConfig.getName());
                    }
                }

                HashMap<Integer, PartitionGroup> partitionGroupMap = Maps.newHashMap(oldTopicConfig.getPartitionGroups());
                partitionGroupMap.put(newPartitionGroup.getGroup(), newPartitionGroup);
                oldTopicConfig.setPartitionGroups(partitionGroupMap);
                break;
            }
            case REMOVE_PARTITION_GROUP: {
                RemovePartitionGroupEvent removePartitionGroupEvent = (RemovePartitionGroupEvent) event;
                TopicConfig oldTopicConfig = cache.getTopicConfigMap().get(removePartitionGroupEvent.getTopic());
                if (oldTopicConfig == null) {
                    logger.warn("topic cache not exist, topic: {}", removePartitionGroupEvent.getTopic());
                    break;
                }

                PartitionGroup partitionGroup = removePartitionGroupEvent.getPartitionGroup();

                for (Integer replica : partitionGroup.getReplicas()) {
                    Map<TopicName, TopicConfig> brokerTopicConfigMap = cache.getTopicConfigBrokerMap().get(replica);
                    if (brokerTopicConfigMap == null) {
                        continue;
                    }
                    boolean isMatch = false;
                    for (Map.Entry<Integer, PartitionGroup> entry : oldTopicConfig.getPartitionGroups().entrySet()) {
                        if (entry.getValue().getGroup() != partitionGroup.getGroup() &&
                                partitionGroup.getReplicas().contains(replica)) {
                            isMatch = true;
                            break;
                        }
                    }
                    if (!isMatch) {
                        brokerTopicConfigMap.remove(oldTopicConfig.getName());
                    }
                }

                HashMap<Integer, PartitionGroup> partitionGroupMap = Maps.newHashMap(oldTopicConfig.getPartitionGroups());
                partitionGroupMap.remove(partitionGroup.getGroup());
                oldTopicConfig.setPartitionGroups(partitionGroupMap);

                break;
            }
            case ADD_PRODUCER: {
                AddProducerEvent addProducerEvent = (AddProducerEvent) event;
                TopicName topic = addProducerEvent.getTopic();
                Producer producer = addProducerEvent.getProducer();

                Map<String, Producer> topicProducerMap = cache.getProducerTopicMap().get(topic);
                if (topicProducerMap == null) {
                    topicProducerMap = Maps.newHashMap();
                    cache.getProducerTopicMap().put(topic, topicProducerMap);
                }
                topicProducerMap.put(producer.getApp(), producer);

                Map<TopicName, Producer> appProducerMap = cache.getProducerAppMap().get(producer.getApp());
                if (appProducerMap == null) {
                    appProducerMap = Maps.newHashMap();
                    cache.getProducerAppMap().put(producer.getApp(), appProducerMap);
                }
                appProducerMap.put(topic, producer);

                cache.getAllProducers().add(producer);
                break;
            }
            case UPDATE_PRODUCER: {
                UpdateProducerEvent updateProducerEvent = (UpdateProducerEvent) event;
                TopicName topic = updateProducerEvent.getTopic();
                Producer producer = updateProducerEvent.getNewProducer();

                Map<String, Producer> topicProducerMap = cache.getProducerTopicMap().get(topic);
                if (topicProducerMap == null) {
                    topicProducerMap = Maps.newHashMap();
                    cache.getProducerTopicMap().put(topic, topicProducerMap);
                }
                topicProducerMap.put(producer.getApp(), producer);

                Map<TopicName, Producer> appProducerMap = cache.getProducerAppMap().get(producer.getApp());
                if (appProducerMap == null) {
                    appProducerMap = Maps.newHashMap();
                    cache.getProducerAppMap().put(producer.getApp(), appProducerMap);
                }
                appProducerMap.put(topic, producer);

                cache.getAllProducers().remove(producer);
                cache.getAllProducers().add(producer);
                break;
            }
            case REMOVE_PRODUCER: {
                RemoveProducerEvent removeProducerEvent = (RemoveProducerEvent) event;
                TopicName topic = removeProducerEvent.getTopic();
                Producer producer = removeProducerEvent.getProducer();

                Map<String, Producer> topicProducerMap = cache.getProducerTopicMap().get(topic);
                if (topicProducerMap != null) {
                    topicProducerMap.remove(producer.getApp());
                }

                Map<TopicName, Producer> appProducerMap = cache.getProducerAppMap().get(producer.getApp());
                if (appProducerMap != null) {
                    appProducerMap.remove(producer.getApp());
                }

                cache.getAllProducers().remove(producer);
                break;
            }
            case ADD_CONSUMER: {
                AddConsumerEvent addConsumerEvent = (AddConsumerEvent) event;
                TopicName topic = addConsumerEvent.getTopic();
                Consumer consumer = addConsumerEvent.getConsumer();

                Map<String, Consumer> topicConsumerMap = cache.getConsumerTopicMap().get(topic);
                if (topicConsumerMap == null) {
                    topicConsumerMap = Maps.newHashMap();
                    cache.getConsumerTopicMap().put(topic, topicConsumerMap);
                }
                topicConsumerMap.put(consumer.getApp(), consumer);

                Map<TopicName, Consumer> appConsumerMap = cache.getConsumerAppMap().get(consumer.getApp());
                if (appConsumerMap == null) {
                    appConsumerMap = Maps.newHashMap();
                    cache.getConsumerAppMap().put(consumer.getApp(), appConsumerMap);
                }
                appConsumerMap.put(topic, consumer);

                cache.getAllConsumers().add(consumer);
                break;
            }
            case UPDATE_CONSUMER: {
                UpdateConsumerEvent updateConsumerEvent = (UpdateConsumerEvent) event;
                TopicName topic = updateConsumerEvent.getTopic();
                Consumer consumer = updateConsumerEvent.getNewConsumer();

                Map<String, Consumer> topicConsumerMap = cache.getConsumerTopicMap().get(topic);
                if (topicConsumerMap == null) {
                    topicConsumerMap = Maps.newHashMap();
                    cache.getConsumerTopicMap().put(topic, topicConsumerMap);
                }
                topicConsumerMap.put(consumer.getApp(), consumer);

                Map<TopicName, Consumer> appConsumerMap = cache.getConsumerAppMap().get(consumer.getApp());
                if (appConsumerMap == null) {
                    appConsumerMap = Maps.newHashMap();
                    cache.getConsumerAppMap().put(consumer.getApp(), appConsumerMap);
                }
                appConsumerMap.put(topic, consumer);

                cache.getAllConsumers().remove(consumer);
                cache.getAllConsumers().add(consumer);
                break;
            }
            case REMOVE_CONSUMER: {
                RemoveConsumerEvent removeConsumerEvent = (RemoveConsumerEvent) event;
                TopicName topic = removeConsumerEvent.getTopic();
                Consumer consumer = removeConsumerEvent.getConsumer();

                Map<String, Consumer> topicConsumerMap = cache.getConsumerTopicMap().get(topic);
                if (topicConsumerMap != null) {
                    topicConsumerMap.remove(consumer.getApp());
                }

                Map<TopicName, Consumer> appConsumerMap = cache.getConsumerAppMap().get(consumer.getApp());
                if (appConsumerMap != null) {
                    appConsumerMap.remove(topic);
                }

                cache.getAllConsumers().remove(consumer);
                break;
            }
            case UPDATE_BROKER: {
                UpdateBrokerEvent updateBrokerEvent = (UpdateBrokerEvent) event;
                Broker newBroker = updateBrokerEvent.getNewBroker();
                cache.getBrokerMap().put(newBroker.getId(), newBroker);
                break;
            }
        }
        return true;
    }

    protected void doOnEvent(MetaEvent event) {
        NameServerEvent nameServerEvent = new NameServerEvent();
        nameServerEvent.setMetaEvent(event);
        nameServerEvent.setEventType(event.getEventType());
        eventBus.inform(nameServerEvent);
    }
}