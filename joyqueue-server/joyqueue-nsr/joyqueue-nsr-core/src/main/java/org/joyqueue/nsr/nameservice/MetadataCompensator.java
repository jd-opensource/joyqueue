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

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.Config;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.event.NameServerEvent;
import org.joyqueue.nsr.config.NameServiceConfig;
import org.joyqueue.nsr.event.AddBrokerEvent;
import org.joyqueue.nsr.event.AddConfigEvent;
import org.joyqueue.nsr.event.AddConsumerEvent;
import org.joyqueue.nsr.event.AddDataCenterEvent;
import org.joyqueue.nsr.event.AddPartitionGroupEvent;
import org.joyqueue.nsr.event.AddProducerEvent;
import org.joyqueue.nsr.event.AddTopicEvent;
import org.joyqueue.nsr.event.CompensateEvent;
import org.joyqueue.nsr.event.RemoveBrokerEvent;
import org.joyqueue.nsr.event.RemoveConfigEvent;
import org.joyqueue.nsr.event.RemoveConsumerEvent;
import org.joyqueue.nsr.event.RemoveDataCenterEvent;
import org.joyqueue.nsr.event.RemovePartitionGroupEvent;
import org.joyqueue.nsr.event.RemoveProducerEvent;
import org.joyqueue.nsr.event.RemoveTopicEvent;
import org.joyqueue.nsr.event.UpdateBrokerEvent;
import org.joyqueue.nsr.event.UpdateConfigEvent;
import org.joyqueue.nsr.event.UpdateConsumerEvent;
import org.joyqueue.nsr.event.UpdateDataCenterEvent;
import org.joyqueue.nsr.event.UpdatePartitionGroupEvent;
import org.joyqueue.nsr.event.UpdateProducerEvent;
import org.joyqueue.nsr.event.UpdateTopicEvent;
import org.joyqueue.nsr.util.DCWrapper;
import org.joyqueue.toolkit.concurrent.EventBus;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * NameServiceCompensator
 * author: gaohaoxiang
 * date: 2019/8/30
 */
public class MetadataCompensator extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(MetadataCompensator.class);

    private NameServiceConfig config;
    private EventBus<NameServerEvent> eventBus;
    private int brokerId = -1;

    public MetadataCompensator(NameServiceConfig config, EventBus<NameServerEvent> eventBus) {
        this.config = config;
        this.eventBus = eventBus;
    }

    public int getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(int brokerId) {
        this.brokerId = brokerId;
    }

    public void compensate(AllMetadataCache oldCache, AllMetadataCache newCache) {
        if (brokerId <= 0) {
            return;
        }
        if (config.getCompensationBrokerEnable()) {
            compensateBroker(oldCache, newCache);
        }
        if (config.getCompensationTopicEnable()) {
            compensateTopic(oldCache, newCache);
        }
        if (config.getCompensationProducerEnable()) {
            compensateProducer(oldCache, newCache);
        }
        if (config.getCompensationConsumerEnable()) {
            compensateConsumer(oldCache, newCache);
        }
        if (config.getCompensationDataCenterEnable()) {
            compensateDataCenter(oldCache, newCache);
        }
        if (config.getCompensationConfigEnable()) {
            compensateConfig(oldCache, newCache);
        }
        if (config.getCompensationEventEnable()) {
            publishEvent(new CompensateEvent(oldCache, newCache));
        }
    }

    protected void compensateTopic(AllMetadataCache oldCache, AllMetadataCache newCache) {
        for (Map.Entry<TopicName, TopicConfig> currentTopicEntry : newCache.getTopicConfigMap().entrySet()) {
            TopicConfig newTopicConfig = currentTopicEntry.getValue();
            TopicConfig oldTopicConfig = oldCache.getTopicConfigMap().get(currentTopicEntry.getKey());

            // 新增topic
            if (oldTopicConfig == null) {
                if (newTopicConfig.isReplica(brokerId)) {
                    publishEvent(new AddTopicEvent(newTopicConfig, Lists.newArrayList(newTopicConfig.getPartitionGroups().values())));

                    for (PartitionGroup partitionGroup : newTopicConfig.fetchTopicPartitionGroupsByBrokerId(brokerId)) {
                        publishEvent(new AddPartitionGroupEvent(newTopicConfig.getName(), partitionGroup));
                    }
                }
            } else {
                // topic副本变化
                if (oldTopicConfig.isReplica(brokerId) && !newTopicConfig.isReplica(brokerId)) {
                    for (PartitionGroup partitionGroup : oldTopicConfig.fetchTopicPartitionGroupsByBrokerId(brokerId)) {
                        publishEvent(new RemovePartitionGroupEvent(oldTopicConfig.getName(), partitionGroup));
                    }
                } else {
                    if (newTopicConfig.isReplica(brokerId)) {
                        // 更新topic
                        if (!compareTopic(oldTopicConfig, newTopicConfig)) {
                            publishEvent(new UpdateTopicEvent(oldTopicConfig, newTopicConfig));
                        }

                        // 更新partitionGroup
                        for (PartitionGroup newPartitionGroup : newTopicConfig.fetchTopicPartitionGroupsByBrokerId(brokerId)) {
                            PartitionGroup oldPartitionGroup = oldTopicConfig.getPartitionGroups().get(newPartitionGroup.getGroup());
                            if (oldPartitionGroup == null || !oldPartitionGroup.getReplicas().contains(brokerId)) {
                                publishEvent(new AddPartitionGroupEvent(newTopicConfig.getName(), newPartitionGroup));
                            } else {
                                if (!comparePartitionGroup(oldPartitionGroup, newPartitionGroup)) {
                                    publishEvent(new UpdatePartitionGroupEvent(newTopicConfig.getName(), oldPartitionGroup, newPartitionGroup));
                                }
                            }
                        }

                        // 删除partitionGroup
                        for (PartitionGroup oldPartitionGroup : oldTopicConfig.fetchTopicPartitionGroupsByBrokerId(brokerId)) {
                            PartitionGroup newPartitionGroup = newTopicConfig.getPartitionGroups().get(oldPartitionGroup.getGroup());
                            if (newPartitionGroup == null || !newPartitionGroup.getReplicas().contains(brokerId)) {
                                publishEvent(new RemovePartitionGroupEvent(newTopicConfig.getName(), oldPartitionGroup));
                            }
                        }
                    }
                }
            }
        }

        // 删除topic
        for (Map.Entry<TopicName, TopicConfig> oldTopicEntry : oldCache.getTopicConfigMap().entrySet()) {
            TopicConfig newTopic = newCache.getTopicConfigMap().get(oldTopicEntry.getKey());
            TopicConfig oldTopic = oldTopicEntry.getValue();
            if (newTopic == null && oldTopic.isReplica(brokerId)) {
                for (PartitionGroup partitionGroup : oldTopic.fetchTopicPartitionGroupsByBrokerId(brokerId)) {
                    publishEvent(new RemovePartitionGroupEvent(oldTopic.getName(), partitionGroup));
                }
                publishEvent(new RemoveTopicEvent(oldTopic, Lists.newArrayList(oldTopicEntry.getValue().getPartitionGroups().values())));
            }
        }
    }

    protected void compensateBroker(AllMetadataCache oldCache, AllMetadataCache newCache) {
        for (Map.Entry<Integer, Broker> newBrokerEntry : newCache.getBrokerMap().entrySet()) {
            Broker oldBroker = oldCache.getBrokerMap().get(newBrokerEntry.getKey());

            if (!newBrokerEntry.getValue().getId().equals(brokerId)) {
                continue;
            }

            // 新增broker
            if (oldBroker == null) {
                publishEvent(new AddBrokerEvent(newBrokerEntry.getValue()));
            } else {
                // 更新broker
                if (!compareBroker(oldBroker, newBrokerEntry.getValue())) {
                    publishEvent(new UpdateBrokerEvent(oldBroker, newBrokerEntry.getValue()));
                }
            }
        }

        // 删除broker
        for (Map.Entry<Integer, Broker> oldBrokerEntry : oldCache.getBrokerMap().entrySet()) {
            if (!oldBrokerEntry.getValue().getId().equals(brokerId)) {
                continue;
            }
            if (newCache.getBrokerMap().containsKey(oldBrokerEntry.getKey())) {
                continue;
            }

            publishEvent(new RemoveBrokerEvent(oldBrokerEntry.getValue()));
        }
    }

    protected void compensateProducer(AllMetadataCache oldCache, AllMetadataCache newCache) {
        for (Producer newProducer : newCache.getAllProducers()) {
            TopicConfig newTopicConfig = newCache.getTopicConfigMap().get(newProducer.getTopic());
            TopicConfig oldTopicConfig = oldCache.getTopicConfigMap().get(newProducer.getTopic());

            Map<TopicName, Producer> oldTopicProducerMap = oldCache.getProducerAppMap().get(newProducer.getApp());
            Producer oldProducer = (oldTopicProducerMap == null ? null : oldTopicProducerMap.get(newProducer.getTopic()));

            if (newTopicConfig == null || !newTopicConfig.isReplica(brokerId)) {
                continue;
            }

            if (oldProducer == null) {
                // 新增producer
                publishEvent(new AddProducerEvent(newProducer.getTopic(), newProducer));
            } else {
                if (oldTopicConfig == null || !oldTopicConfig.isReplica(brokerId)) {
                    publishEvent(new AddProducerEvent(newProducer.getTopic(), newProducer));
                } else {
                    // 更新producer
                    if (!compareProducer(oldProducer, newProducer)) {
                        publishEvent(new UpdateProducerEvent(newProducer.getTopic(), oldProducer, newProducer));
                    }
                }
            }
        }

        // 删除producer
        for (Producer oldProducer : oldCache.getAllProducers()) {
            TopicConfig newTopicConfig = newCache.getTopicConfigMap().get(oldProducer.getTopic());
            TopicConfig oldTopicConfig = oldCache.getTopicConfigMap().get(oldProducer.getTopic());

            Map<TopicName, Producer> newTopicProducerMap = newCache.getProducerAppMap().get(oldProducer.getApp());
            Producer newProducer = (newTopicProducerMap == null ? null : newTopicProducerMap.get(oldProducer.getTopic()));

            if (newProducer == null) {
                if (oldTopicConfig != null && oldTopicConfig.isReplica(brokerId)) {
                    publishEvent(new RemoveProducerEvent(oldProducer.getTopic(), oldProducer));
                }
            } else {
                if ((newTopicConfig == null || !newTopicConfig.isReplica(brokerId))
                        && (oldTopicConfig != null && oldTopicConfig.isReplica(brokerId))) {
                    publishEvent(new RemoveProducerEvent(oldProducer.getTopic(), oldProducer));
                }
            }
        }
    }

    protected void compensateConsumer(AllMetadataCache oldCache, AllMetadataCache newCache) {
        for (Consumer newConsumer : newCache.getAllConsumers()) {
            TopicConfig newTopicConfig = newCache.getTopicConfigMap().get(newConsumer.getTopic());
            TopicConfig oldTopicConfig = oldCache.getTopicConfigMap().get(newConsumer.getTopic());

            Map<TopicName, Consumer> oldTopicConsumerMap = oldCache.getConsumerAppMap().get(newConsumer.getApp());
            Consumer oldConsumer = (oldTopicConsumerMap == null ? null : oldTopicConsumerMap.get(newConsumer.getTopic()));

            if (newTopicConfig == null || !newTopicConfig.isReplica(brokerId)) {
                continue;
            }

            if (oldConsumer == null) {
                // 新增consumer
                publishEvent(new AddConsumerEvent(newConsumer.getTopic(), newConsumer));
            } else {
                if (oldTopicConfig == null || !oldTopicConfig.isReplica(brokerId)) {
                    publishEvent(new AddConsumerEvent(newConsumer.getTopic(), newConsumer));
                } else {
                    // 更新consumer
                    if (!compareConsumer(oldConsumer, newConsumer)) {
                        publishEvent(new UpdateConsumerEvent(newConsumer.getTopic(), oldConsumer, newConsumer));
                    }
                }
            }
        }

        // 删除consumer
        for (Consumer oldConsumer : oldCache.getAllConsumers()) {
            TopicConfig newTopicConfig = newCache.getTopicConfigMap().get(oldConsumer.getTopic());
            TopicConfig oldTopicConfig = oldCache.getTopicConfigMap().get(oldConsumer.getTopic());

            Map<TopicName, Consumer> newTopicConsumerMap = newCache.getConsumerAppMap().get(oldConsumer.getApp());
            Consumer newConsumer = (newTopicConsumerMap == null ? null : newTopicConsumerMap.get(oldConsumer.getTopic()));

            if (newConsumer == null) {
                if (oldTopicConfig != null && oldTopicConfig.isReplica(brokerId)) {
                    publishEvent(new RemoveConsumerEvent(oldConsumer.getTopic(), oldConsumer));
                }
            } else {
                if ((newTopicConfig == null || !newTopicConfig.isReplica(brokerId))
                        && (oldTopicConfig != null && oldTopicConfig.isReplica(brokerId))) {
                    publishEvent(new RemoveConsumerEvent(oldConsumer.getTopic(), oldConsumer));
                }
            }
        }
    }

    protected void compensateDataCenter(AllMetadataCache oldCache, AllMetadataCache newCache) {
        for (DCWrapper newDataCenter : newCache.getAllDataCenters()) {
            DCWrapper oldDataCenter = oldCache.getDataCenterCodeMap().get(newDataCenter.getDataCenter().getCode());
            if (oldDataCenter == null) {
                // 新增dataCenter
                publishEvent(new AddDataCenterEvent(newDataCenter.getDataCenter()));
            } else {
                // 更新dataCenter
                if (!compareDataCenter(oldDataCenter, newDataCenter)) {
                    publishEvent(new UpdateDataCenterEvent(oldDataCenter.getDataCenter(), newDataCenter.getDataCenter()));
                }
            }
        }

        // 删除dataCenter
        for (DCWrapper oldDataCenter : oldCache.getAllDataCenters()) {
            if (newCache.getDataCenterCodeMap().containsKey(oldDataCenter.getDataCenter().getCode())) {
                continue;
            }

            publishEvent(new RemoveDataCenterEvent(oldDataCenter.getDataCenter()));
        }
    }

    protected void compensateConfig(AllMetadataCache oldCache, AllMetadataCache newCache) {
        for (Config newConfig : newCache.getAllConfigs()) {
            Config oldConfig = oldCache.getConfigKeyMap().get(newConfig.getId());
            if (oldConfig == null) {
                // 新增config
                publishEvent(new AddConfigEvent(newConfig));
            } else {
                // 更新config
                if (!compareConfig(oldConfig, newConfig)) {
                    publishEvent(new UpdateConfigEvent(oldConfig, newConfig));
                }
            }
        }

        // 删除config
        for (Config oldConfig : oldCache.getAllConfigs()) {
            if (newCache.getConfigKeyMap().containsKey(oldConfig.getId())) {
                continue;
            }

            publishEvent(new RemoveConfigEvent(oldConfig));
        }
    }

    protected void publishEvent(MetaEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug("publish event, event: {}", JSON.toJSONString(event));
        }

        NameServerEvent nameServerEvent = new NameServerEvent();
        nameServerEvent.setMetaEvent(event);
        nameServerEvent.setEventType(event.getEventType());
        eventBus.inform(nameServerEvent);
    }

    protected boolean compareTopic(TopicConfig topicConfig1, TopicConfig topicConfig2) {
        if (!topicConfig1.getName().equals(topicConfig2.getName())) {
            return false;
        }
        if (topicConfig1.getPartitions() != topicConfig2.getPartitions()) {
            return false;
        }
        if (!Objects.equals(topicConfig1.getPolicy(), topicConfig2.getPolicy())) {
            return false;
        }
        return true;
    }

    protected boolean comparePartitionGroup(PartitionGroup partitionGroup1, PartitionGroup partitionGroup2) {
        return partitionGroup1.equals(partitionGroup2);
    }

    protected boolean compareBroker(Broker broker1, Broker broker2) {
        return broker1.equals(broker2);
    }

    protected boolean compareProducer(Producer producer1, Producer producer2) {
        return producer1.equals(producer2);
    }

    protected boolean compareConsumer(Consumer consumer1, Consumer consumer2) {
        return consumer1.equals(consumer2);
    }

    protected boolean compareDataCenter(DCWrapper dataCenter1, DCWrapper dataCenter2) {
        return StringUtils.equals(dataCenter1.getDataCenter().getName(), dataCenter2.getDataCenter().getName())
                && StringUtils.equals(dataCenter1.getDataCenter().getRegion(), dataCenter2.getDataCenter().getRegion())
                && StringUtils.equals(dataCenter1.getDataCenter().getUrl(), dataCenter2.getDataCenter().getUrl());
    }

    protected boolean compareConfig(Config config1, Config config2) {
        return config1.equals(config2);
    }
}