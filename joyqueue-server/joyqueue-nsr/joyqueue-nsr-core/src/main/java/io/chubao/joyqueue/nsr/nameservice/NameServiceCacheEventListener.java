package io.chubao.joyqueue.nsr.nameservice;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.Topic;
import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.nsr.config.NameServiceConfig;
import io.chubao.joyqueue.nsr.event.AddConsumerEvent;
import io.chubao.joyqueue.nsr.event.AddPartitionGroupEvent;
import io.chubao.joyqueue.nsr.event.AddProducerEvent;
import io.chubao.joyqueue.nsr.event.AddTopicEvent;
import io.chubao.joyqueue.nsr.event.RemoveConsumerEvent;
import io.chubao.joyqueue.nsr.event.RemovePartitionGroupEvent;
import io.chubao.joyqueue.nsr.event.RemoveProducerEvent;
import io.chubao.joyqueue.nsr.event.RemoveTopicEvent;
import io.chubao.joyqueue.nsr.event.UpdateBrokerEvent;
import io.chubao.joyqueue.nsr.event.UpdateConsumerEvent;
import io.chubao.joyqueue.nsr.event.UpdatePartitionGroupEvent;
import io.chubao.joyqueue.nsr.event.UpdateProducerEvent;
import io.chubao.joyqueue.nsr.event.UpdateTopicEvent;
import io.chubao.joyqueue.nsr.message.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NameServiceCacheEventListener
 * author: gaohaoxiang
 * date: 2019/9/4
 */
public class NameServiceCacheEventListener implements MessageListener<MetaEvent> {

    protected static final Logger logger = LoggerFactory.getLogger(NameServiceCacheEventListener.class);

    private NameServiceConfig config;
    private NameServiceCacheManager nameServiceCacheManager;

    public NameServiceCacheEventListener(NameServiceConfig config, NameServiceCacheManager nameServiceCacheManager) {
        this.config = config;
        this.nameServiceCacheManager = nameServiceCacheManager;
    }

    @Override
    public void onEvent(MetaEvent event) {
        // 不是原子操作
        // 删除，修改topic, producer，consumer需要优化
        NameServiceCache cache = nameServiceCacheManager.getCache();
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
        cache.updateLastTimestamp();
    }
}