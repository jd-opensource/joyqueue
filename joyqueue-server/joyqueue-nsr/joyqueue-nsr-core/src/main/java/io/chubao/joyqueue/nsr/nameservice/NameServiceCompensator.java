package io.chubao.joyqueue.nsr.nameservice;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.event.NameServerEvent;
import io.chubao.joyqueue.nsr.config.NameServiceConfig;
import io.chubao.joyqueue.nsr.event.AddBrokerEvent;
import io.chubao.joyqueue.nsr.event.AddConfigEvent;
import io.chubao.joyqueue.nsr.event.AddConsumerEvent;
import io.chubao.joyqueue.nsr.event.AddDataCenterEvent;
import io.chubao.joyqueue.nsr.event.AddPartitionGroupEvent;
import io.chubao.joyqueue.nsr.event.AddProducerEvent;
import io.chubao.joyqueue.nsr.event.AddTopicEvent;
import io.chubao.joyqueue.nsr.event.RemoveBrokerEvent;
import io.chubao.joyqueue.nsr.event.RemoveConfigEvent;
import io.chubao.joyqueue.nsr.event.RemoveConsumerEvent;
import io.chubao.joyqueue.nsr.event.RemoveDataCenterEvent;
import io.chubao.joyqueue.nsr.event.RemovePartitionGroupEvent;
import io.chubao.joyqueue.nsr.event.RemoveProducerEvent;
import io.chubao.joyqueue.nsr.event.RemoveTopicEvent;
import io.chubao.joyqueue.nsr.event.UpdateBrokerEvent;
import io.chubao.joyqueue.nsr.event.UpdateConfigEvent;
import io.chubao.joyqueue.nsr.event.UpdateConsumerEvent;
import io.chubao.joyqueue.nsr.event.UpdateDataCenterEvent;
import io.chubao.joyqueue.nsr.event.UpdatePartitionGroupEvent;
import io.chubao.joyqueue.nsr.event.UpdateProducerEvent;
import io.chubao.joyqueue.nsr.event.UpdateTopicEvent;
import io.chubao.joyqueue.nsr.util.DCWrapper;
import io.chubao.joyqueue.toolkit.concurrent.EventBus;
import io.chubao.joyqueue.toolkit.service.Service;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * NameServiceCompensator
 * author: gaohaoxiang
 * date: 2019/8/30
 */
public class NameServiceCompensator extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(NameServiceCompensator.class);

    private NameServiceConfig config;
    private EventBus<NameServerEvent> eventBus;
    private int brokerId = -1;

    public NameServiceCompensator(NameServiceConfig config, EventBus<NameServerEvent> eventBus) {
        this.config = config;
        this.eventBus = eventBus;
    }

    public void setBrokerId(int brokerId) {
        this.brokerId = brokerId;
    }

    public void compensate(NameServiceCache oldCache, NameServiceCache newCache) {
        if (brokerId <= 0) {
            return;
        }
        if (config.getCompensationTopicEnable()) {
            compensateTopic(oldCache, newCache);
        }
        if (config.getCompensationBrokerEnable()) {
            compensateBroker(oldCache, newCache);
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
    }

    protected void compensateTopic(NameServiceCache oldCache, NameServiceCache newCache) {
        for (Map.Entry<TopicName, TopicConfig> currentTopicEntry : newCache.getTopicConfigMap().entrySet()) {
            TopicConfig newTopicConfig = currentTopicEntry.getValue();
            TopicConfig oldTopicConfig = oldCache.getTopicConfigMap().get(currentTopicEntry.getKey());

            // 新增topic
            if (oldTopicConfig == null) {
                if (newTopicConfig.isReplica(brokerId)) {
                    publishEvent(new AddTopicEvent(newTopicConfig, Lists.newArrayList(newTopicConfig.getPartitionGroups().values())));

                    for (Map.Entry<Integer, PartitionGroup> entry : newTopicConfig.getPartitionGroups().entrySet()) {
                        publishEvent(new AddPartitionGroupEvent(newTopicConfig.getName(), entry.getValue()));
                    }
                }
            } else {
                // topic副本变化
                if (oldTopicConfig.isReplica(brokerId) && !newTopicConfig.isReplica(brokerId)) {
                    for (PartitionGroup partitionGroup : oldTopicConfig.fetchPartitionGroupByBrokerId(brokerId)) {
                        publishEvent(new RemovePartitionGroupEvent(oldTopicConfig.getName(), partitionGroup));
                    }
                } else {
                    if (newTopicConfig.isReplica(brokerId)) {
                        // 更新topic
                        if (!compareTopic(oldTopicConfig, newTopicConfig)) {
                            publishEvent(new UpdateTopicEvent(oldTopicConfig, newTopicConfig));
                        }

                        // 更新partitionGroup
                        for (PartitionGroup newPartition : newTopicConfig.fetchPartitionGroupByBrokerId(brokerId)) {
                            PartitionGroup oldPartitionGroup = oldTopicConfig.getPartitionGroups().get(newPartition.getGroup());
                            if (oldPartitionGroup == null || !oldPartitionGroup.getReplicas().contains(brokerId)) {
                                publishEvent(new AddPartitionGroupEvent(newTopicConfig.getName(), oldPartitionGroup));
                            } else {
                                if (!comparePartitionGroup(oldPartitionGroup, newPartition)) {
                                    publishEvent(new UpdatePartitionGroupEvent(newTopicConfig.getName(), oldPartitionGroup, newPartition));
                                }
                            }
                        }

                        // 删除partitionGroup
                        for (PartitionGroup oldPartitionGroup : oldTopicConfig.fetchPartitionGroupByBrokerId(brokerId)) {
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
            if (oldTopicEntry.getValue().isReplica(brokerId) && newTopic == null) {
                publishEvent(new RemoveTopicEvent(oldTopicEntry.getValue(), Lists.newArrayList(oldTopicEntry.getValue().getPartitionGroups().values())));
            }
        }
    }

    protected void compensateBroker(NameServiceCache oldCache, NameServiceCache newCache) {
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

    protected void compensateProducer(NameServiceCache oldCache, NameServiceCache newCache) {
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
                publishEvent(new RemoveProducerEvent(oldProducer.getTopic(), oldProducer));
            } else if ((newTopicConfig == null || !newTopicConfig.isReplica(brokerId))
                    && (oldTopicConfig != null && oldTopicConfig.isReplica(brokerId))) {

                publishEvent(new RemoveProducerEvent(oldProducer.getTopic(), oldProducer));
            }
        }
    }

    protected void compensateConsumer(NameServiceCache oldCache, NameServiceCache newCache) {
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
                publishEvent(new RemoveConsumerEvent(oldConsumer.getTopic(), oldConsumer));
            } else if ((newTopicConfig == null || !newTopicConfig.isReplica(brokerId))
                    && (oldTopicConfig != null && oldTopicConfig.isReplica(brokerId))) {

                publishEvent(new RemoveConsumerEvent(oldConsumer.getTopic(), oldConsumer));
            }
        }
    }

    protected void compensateDataCenter(NameServiceCache oldCache, NameServiceCache newCache) {
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

    protected void compensateConfig(NameServiceCache oldCache, NameServiceCache newCache) {
        for (Config newConfig : newCache.getAllConfigs()) {
            Config oldConfig = oldCache.getConfigKeyMap().get(newConfig.getKey());
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
            if (newCache.getConfigKeyMap().containsKey(oldConfig.getKey())) {
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
        eventBus.add(nameServerEvent);
    }

    protected boolean compareTopic(TopicConfig topicConfig1, TopicConfig topicConfig2) {
        if (!topicConfig1.getName().equals(topicConfig2.getName())) {
            return false;
        }
        if (topicConfig1.getPartitions() != topicConfig2.getPartitions()) {
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