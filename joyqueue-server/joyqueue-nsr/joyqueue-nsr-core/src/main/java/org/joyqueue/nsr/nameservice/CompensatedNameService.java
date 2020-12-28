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
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import org.apache.commons.collections.MapUtils;
import org.joyqueue.domain.AllMetadata;
import org.joyqueue.domain.AppToken;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.ClientType;
import org.joyqueue.domain.Config;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.DataCenter;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.Replica;
import org.joyqueue.domain.Subscription;
import org.joyqueue.domain.Topic;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.NameServerEvent;
import org.joyqueue.nsr.NameService;
import org.joyqueue.nsr.config.NameServiceConfig;
import org.joyqueue.nsr.exception.NsrException;
import org.joyqueue.nsr.message.Messenger;
import org.joyqueue.toolkit.concurrent.EventBus;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.config.PropertySupplierAware;
import org.joyqueue.toolkit.lang.LifeCycle;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CompensatedNameService
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class CompensatedNameService extends Service implements NameService, PropertySupplierAware {

    protected static final Logger logger = LoggerFactory.getLogger(CompensatedNameService.class);

    private final EventBus<NameServerEvent> eventBus = new EventBus("joyqueue-compensated-nameservice-eventBus");
    private final ExtensionPoint<Messenger, String> serviceProviderPoint = new ExtensionPointLazy<>(Messenger.class);

    private NameServiceConfig config;
    private NameService delegate;

    private PropertySupplier supplier;
    private Messenger messenger;
    private MetadataCacheManager metadataCacheManager;
    private MetadataCompensator metadataCompensator;
    private CompensateMetadataThread compensateMetadataThread;
    private int brokerId;

    public CompensatedNameService(NameService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.supplier = supplier;
        this.config = new NameServiceConfig(supplier);
        this.messenger = serviceProviderPoint.get(config.getMessengerType());
        this.metadataCacheManager = new MetadataCacheManager(config);
        this.metadataCompensator = new MetadataCompensator(config, eventBus);
        this.compensateMetadataThread = new CompensateMetadataThread(config, delegate, metadataCacheManager, metadataCompensator);

        try {
            enrichIfNecessary(messenger);
            delegate.start();
            eventBus.start();

            metadataCacheManager.start();
            metadataCompensator.start();
            compensateMetadataThread.doCompensate();
        } catch (Exception e) {
            throw new NsrException(e);
        }
    }

    @Override
    protected void doStart() throws Exception {
        try {
            compensateMetadataThread.start();
        } catch (Exception e) {
            throw new NsrException(e);
        }
        messenger.addListener(new MetadataCacheEventListener(config, eventBus, metadataCacheManager));
    }

    @Override
    protected void doStop() {
        compensateMetadataThread.stop();
        metadataCompensator.stop();
        metadataCacheManager.stop();
        delegate.stop();
        messenger.stop();
    }

    protected  <T> T enrichIfNecessary(T obj) throws Exception {
        if (obj instanceof LifeCycle) {
            if (((LifeCycle) obj).isStarted()) {
                return obj;
            }
        }
        if (obj instanceof PropertySupplierAware) {
            ((PropertySupplierAware) obj).setSupplier(supplier);
        }
        if (obj instanceof LifeCycle) {
            ((LifeCycle) obj).start();
        }
        return obj;
    }

    @Override
    public TopicConfig subscribe(Subscription subscription, ClientType clientType) {
        return delegate.subscribe(subscription, clientType);
    }

    @Override
    public List<TopicConfig> subscribe(List<Subscription> subscriptions, ClientType clientType) {
        return delegate.subscribe(subscriptions, clientType);
    }

    @Override
    public void unSubscribe(Subscription subscription) {
        delegate.unSubscribe(subscription);
    }

    @Override
    public void unSubscribe(List<Subscription> subscriptions) {
        delegate.unSubscribe(subscriptions);
    }

    @Override
    public boolean hasSubscribe(String app, Subscription.Type subscribe) {
        switch (subscribe) {
            case CONSUMPTION: {
                return MapUtils.isNotEmpty(metadataCacheManager.getConsumerByApp(app));
            }
            case PRODUCTION: {
                return MapUtils.isNotEmpty(metadataCacheManager.getProducerByApp(app));
            }
        }
        return false;
    }

    @Override
    public void leaderReport(TopicName topic, int partitionGroup, int leaderBrokerId, Set<Integer> isrId, int termId) {
        delegate.leaderReport(topic, partitionGroup, leaderBrokerId, isrId, termId);
    }

    @Override
    public Broker getBroker(int brokerId) {
        return metadataCacheManager.getBroker(brokerId);
    }

    @Override
    public List<Broker> getAllBrokers() {
        return metadataCacheManager.getAllBrokers();
    }

    @Override
    public void addTopic(Topic topic, List<PartitionGroup> partitionGroups) {
        delegate.addTopic(topic, partitionGroups);
    }

    @Override
    public TopicConfig getTopicConfig(TopicName topic) {
        return metadataCacheManager.getTopicConfig(topic);
    }

    @Override
    public Set<String> getAllTopicCodes() {
        return metadataCacheManager.getAllTopicCodes();
    }

    @Override
    public Set<String> getTopics(String app, Subscription.Type subscription) {
        return metadataCacheManager.getTopics(app, subscription);
    }

    @Override
    public Map<TopicName, TopicConfig> getTopicConfigByBroker(Integer brokerId) {
        return metadataCacheManager.getTopicConfigByBroker(brokerId);
    }

    @Override
    public Broker register(Integer brokerId, String brokerIp, Integer port) {
        Broker broker = delegate.register(brokerId, brokerIp, port);
        if (broker != null) {
            this.brokerId = broker.getId();
            this.metadataCompensator.setBrokerId(this.brokerId);
        }
        return broker;
    }

    @Override
    public Producer getProducerByTopicAndApp(TopicName topic, String app) {
        return metadataCacheManager.getProducerByTopicAndApp(topic, app);
    }

    @Override
    public Consumer getConsumerByTopicAndApp(TopicName topic, String app) {
        return metadataCacheManager.getConsumerByTopicAndApp(topic, app);
    }

    @Override
    public Map<TopicName, TopicConfig> getTopicConfigByApp(String subscribeApp, Subscription.Type subscribe) {
        return metadataCacheManager.getTopicConfigByApp(subscribeApp, subscribe);
    }

    @Override
    public DataCenter getDataCenter(String ip) {
        return metadataCacheManager.getDataCenter(ip);
    }

    @Override
    public String getConfig(String group, String key) {
        return metadataCacheManager.getConfig(group, key);
    }

    @Override
    public List<Config> getAllConfigs() {
        return metadataCacheManager.getAllConfigs();
    }

    @Override
    public List<Broker> getBrokerByRetryType(String retryType) {
        return metadataCacheManager.getBrokerByRetryType(retryType);
    }

    @Override
    public List<Consumer> getConsumerByTopic(TopicName topic) {
        return metadataCacheManager.getConsumerByTopic(topic);
    }

    @Override
    public List<Consumer> getConsumersByApp(String app) {
        Map<TopicName, Consumer> consumerByApp = metadataCacheManager.getConsumerByApp(app);
        if (MapUtils.isEmpty(consumerByApp)) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(consumerByApp.values());
    }

    @Override
    public List<Producer> getProducerByTopic(TopicName topic) {
        return metadataCacheManager.getProducerByTopic(topic);
    }

    @Override
    public List<Producer> getProducersByApp(String app) {
        Map<TopicName, Producer> producerByApp = metadataCacheManager.getProducerByApp(app);
        if (MapUtils.isEmpty(producerByApp)) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(producerByApp.values());
    }

    @Override
    public List<Replica> getReplicaByBroker(Integer brokerId) {
        return metadataCacheManager.getReplicaByBroker(brokerId);
    }

    @Override
    public AppToken getAppToken(String app, String token) {
        return metadataCacheManager.getAppToken(app, token);
    }

    @Override
    public AllMetadata getAllMetadata() {
        return delegate.getAllMetadata();
    }

    @Override
    public void addListener(EventListener<NameServerEvent> listener) {
        eventBus.addListener(listener);
    }

    @Override
    public void removeListener(EventListener<NameServerEvent> listener) {
        eventBus.removeListener(listener);
    }

    @Override
    public void addEvent(NameServerEvent event) {
        eventBus.add(event);
    }
    public NameService getDelegate() {
        return delegate;
    }
}