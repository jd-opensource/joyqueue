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

import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
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
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
    private NameServiceCacheManager nameServiceCacheManager;
    private NameServiceCompensator nameServiceCompensator;
    private NameServiceCompensateThread nameServiceCompensateThread;
    private int brokerId;
    private volatile long nameserverLastAvailableTime = 0;
    private AtomicInteger nameserverNotAvailableCounter = new AtomicInteger(0);

    public CompensatedNameService(NameService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.supplier = supplier;
        this.config = new NameServiceConfig(supplier);
        this.messenger = serviceProviderPoint.get(config.getMessengerType());
        this.nameServiceCacheManager = new NameServiceCacheManager(config);
        this.nameServiceCompensator = new NameServiceCompensator(config, eventBus);
        this.nameServiceCompensateThread = new NameServiceCompensateThread(config, delegate, nameServiceCacheManager, nameServiceCompensator);

        try {
            enrichIfNecessary(messenger);
            delegate.start();
            eventBus.start();

            nameServiceCacheManager.start();
            nameServiceCompensator.start();
            nameServiceCompensateThread.doCompensate();
        } catch (Exception e) {
            throw new NsrException(e);
        }
    }

    @Override
    protected void doStart() throws Exception {
        try {
            nameServiceCompensateThread.start();
        } catch (Exception e) {
            throw new NsrException(e);
        }
        messenger.addListener(new NameServiceCacheEventListener(config, eventBus, nameServiceCacheManager));
    }

    @Override
    protected void doStop() {
        nameServiceCompensateThread.stop();
        nameServiceCompensator.stop();
        nameServiceCacheManager.stop();
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
        return delegate.hasSubscribe(app, subscribe);
    }

    @Override
    public void leaderReport(TopicName topic, int partitionGroup, int leaderBrokerId, Set<Integer> isrId, int termId) {
        delegate.leaderReport(topic, partitionGroup, leaderBrokerId, isrId, termId);
    }

    @Override
    public Broker getBroker(int brokerId) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getBroker(brokerId);
        }
        if (config.getCompensationErrorCacheEnable() && !nameserverIsAvailable()) {
            return nameServiceCacheManager.getBroker(brokerId);
        }
        try {
            Broker broker = delegate.getBroker(brokerId);
            setNameserverAvailable();
            return broker;
        } catch (Exception e) {
            logger.error("gerBroker exception, brokerId: {}", brokerId, e);
            setNameserverNotAvailable();
            if (config.getCompensationErrorCacheEnable()) {
                return nameServiceCacheManager.getBroker(brokerId);
            }
            throw new NsrException(e);
        }
    }

    @Override
    public List<Broker> getAllBrokers() {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getAllBrokers();
        }
        try {
            List<Broker> allBrokers = delegate.getAllBrokers();
            setNameserverAvailable();
            return allBrokers;
        } catch (Exception e) {
            logger.error("getAllBrokers exception", e);
            setNameserverNotAvailable();
            if (config.getCompensationErrorCacheEnable()) {
                return nameServiceCacheManager.getAllBrokers();
            }
            throw new NsrException(e);
        }
    }

    @Override
    public void addTopic(Topic topic, List<PartitionGroup> partitionGroups) {
        delegate.addTopic(topic, partitionGroups);
    }

    @Override
    public TopicConfig getTopicConfig(TopicName topic) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getTopicConfig(topic);
        }
        if (config.getCompensationErrorCacheEnable() && !nameserverIsAvailable()) {
            return nameServiceCacheManager.getTopicConfig(topic);
        }
        try {
            TopicConfig topicConfig = delegate.getTopicConfig(topic);
            setNameserverAvailable();
            return topicConfig;
        } catch (Exception e) {
            logger.error("getTopicConfig exception, topic: {}", topic, e);
            setNameserverNotAvailable();
            if (config.getCompensationErrorCacheEnable()) {
                return nameServiceCacheManager.getTopicConfig(topic);
            }
            throw new NsrException(e);
        }
    }

    @Override
    public Set<String> getAllTopicCodes() {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getAllTopicCodes();
        }
        if (config.getCompensationErrorCacheEnable() && !nameserverIsAvailable()) {
            return nameServiceCacheManager.getAllTopicCodes();
        }
        try {
            Set<String> allTopicCodes = delegate.getAllTopicCodes();
            setNameserverAvailable();
            return allTopicCodes;
        } catch (Exception e) {
            logger.error("getAllTopicCodes exception", e);
            setNameserverNotAvailable();
            if (config.getCompensationErrorCacheEnable()) {
                return nameServiceCacheManager.getAllTopicCodes();
            }
            throw new NsrException(e);
        }
    }

    @Override
    public Set<String> getTopics(String app, Subscription.Type subscription) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getTopics(app, subscription);
        }
        if (config.getCompensationErrorCacheEnable() && !nameserverIsAvailable()) {
            return nameServiceCacheManager.getTopics(app, subscription);
        }
        try {
            Set<String> topics = delegate.getTopics(app, subscription);
            setNameserverAvailable();
            return topics;
        } catch (Exception e) {
            logger.error("getTopics exception, app: {}, subscription: {}", app, subscription, e);
            setNameserverNotAvailable();
            if (config.getCompensationErrorCacheEnable()) {
                return nameServiceCacheManager.getTopics(app, subscription);
            }
            throw new NsrException(e);
        }
    }

    @Override
    public Map<TopicName, TopicConfig> getTopicConfigByBroker(Integer brokerId) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getTopicConfigByBroker(brokerId);
        }
        if (config.getCompensationErrorCacheEnable() && !nameserverIsAvailable()) {
            return nameServiceCacheManager.getTopicConfigByBroker(brokerId);
        }
        try {
            Map<TopicName, TopicConfig> topicConfigByBroker = delegate.getTopicConfigByBroker(brokerId);
            setNameserverAvailable();
            return topicConfigByBroker;
        } catch (Exception e) {
            logger.error("getTopicConfigByBroker exception, brokerId: {}", brokerId, e);
            setNameserverNotAvailable();
            if (config.getCompensationErrorCacheEnable()) {
                return nameServiceCacheManager.getTopicConfigByBroker(brokerId);
            }
            throw new NsrException(e);
        }
    }

    @Override
    public Broker register(Integer brokerId, String brokerIp, Integer port) {
        Broker broker = delegate.register(brokerId, brokerIp, port);
        if (broker != null) {
            this.brokerId = broker.getId();
            this.nameServiceCompensator.setBrokerId(this.brokerId);
        }
        return broker;
    }

    @Override
    public Producer getProducerByTopicAndApp(TopicName topic, String app) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getProducerByTopicAndApp(topic, app);
        }
        if (config.getCompensationErrorCacheEnable() && !nameserverIsAvailable()) {
            return nameServiceCacheManager.getProducerByTopicAndApp(topic, app);
        }
        try {
            Producer producerByTopicAndApp = delegate.getProducerByTopicAndApp(topic, app);
            setNameserverAvailable();
            return producerByTopicAndApp;
        } catch (Exception e) {
            logger.error("getProducerByTopicAndApp exception, topic: {}, app: {}", topic, app, e);
            setNameserverNotAvailable();
            if (config.getCompensationErrorCacheEnable()) {
                return nameServiceCacheManager.getProducerByTopicAndApp(topic, app);
            }
            throw new NsrException(e);
        }
    }

    @Override
    public Consumer getConsumerByTopicAndApp(TopicName topic, String app) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getConsumerByTopicAndApp(topic, app);
        }
        if (config.getCompensationErrorCacheEnable() && !nameserverIsAvailable()) {
            return nameServiceCacheManager.getConsumerByTopicAndApp(topic, app);
        }
        try {
            Consumer consumerByTopicAndApp = delegate.getConsumerByTopicAndApp(topic, app);
            setNameserverAvailable();
            return consumerByTopicAndApp;
        } catch (Exception e) {
            logger.error("getConsumerByTopicAndApp exception, topic: {}, app: {}", topic, app, e);
            setNameserverNotAvailable();
            if (config.getCompensationErrorCacheEnable()) {
                return nameServiceCacheManager.getConsumerByTopicAndApp(topic, app);
            }
            throw new NsrException(e);
        }
    }

    @Override
    public Map<TopicName, TopicConfig> getTopicConfigByApp(String subscribeApp, Subscription.Type subscribe) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getTopicConfigByApp(subscribeApp, subscribe);
        }
        if (config.getCompensationErrorCacheEnable() && !nameserverIsAvailable()) {
            return nameServiceCacheManager.getTopicConfigByApp(subscribeApp, subscribe);
        }
        try {
            Map<TopicName, TopicConfig> topicConfigByApp = delegate.getTopicConfigByApp(subscribeApp, subscribe);
            setNameserverAvailable();
            return topicConfigByApp;
        } catch (Exception e) {
            logger.error("getTopicConfigByApp exception, subscribeApp: {}, subscribe: {}", subscribeApp, subscribe, e);
            setNameserverNotAvailable();
            if (config.getCompensationErrorCacheEnable()) {
                return nameServiceCacheManager.getTopicConfigByApp(subscribeApp, subscribe);
            }
            throw new NsrException(e);
        }
    }

    @Override
    public DataCenter getDataCenter(String ip) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getDataCenter(ip);
        }
        if (config.getCompensationErrorCacheEnable() && !nameserverIsAvailable()) {
            return nameServiceCacheManager.getDataCenter(ip);
        }
        try {
            DataCenter dataCenter = delegate.getDataCenter(ip);
            setNameserverAvailable();
            return dataCenter;
        } catch (Exception e) {
            logger.error("getDataCenter exception, ip: {}", ip, e);
            setNameserverNotAvailable();
            if (config.getCompensationErrorCacheEnable()) {
                return nameServiceCacheManager.getDataCenter(ip);
            }
            throw new NsrException(e);
        }
    }

    @Override
    public String getConfig(String group, String key) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getConfig(group, key);
        }
        if (config.getCompensationErrorCacheEnable() && !nameserverIsAvailable()) {
            return nameServiceCacheManager.getConfig(group, key);
        }
        try {
            String config = delegate.getConfig(group, key);
            setNameserverAvailable();
            return config;
        } catch (Exception e) {
            logger.error("getConfig exception, group: {}, key: {}", group, key, e);
            setNameserverNotAvailable();
            if (config.getCompensationErrorCacheEnable()) {
                return nameServiceCacheManager.getConfig(group, key);
            }
            throw new NsrException(e);
        }
    }

    @Override
    public List<Config> getAllConfigs() {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getAllConfigs();
        }
        if (config.getCompensationErrorCacheEnable() && !nameserverIsAvailable()) {
            return nameServiceCacheManager.getAllConfigs();
        }
        try {
            List<Config> allConfigs = delegate.getAllConfigs();
            setNameserverAvailable();
            return allConfigs;
        } catch (Exception e) {
            logger.error("getAllConfigs exception", e);
            setNameserverNotAvailable();
            if (config.getCompensationErrorCacheEnable()) {
                return nameServiceCacheManager.getAllConfigs();
            }
            throw new NsrException(e);
        }
    }

    @Override
    public List<Broker> getBrokerByRetryType(String retryType) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getBrokerByRetryType(retryType);
        }
        if (config.getCompensationErrorCacheEnable() && !nameserverIsAvailable()) {
            return nameServiceCacheManager.getBrokerByRetryType(retryType);
        }
        try {
            List<Broker> brokerByRetryType = delegate.getBrokerByRetryType(retryType);
            setNameserverAvailable();
            return brokerByRetryType;
        } catch (Exception e) {
            logger.error("getBrokerByRetryType exception, retryType: {}", retryType, e);
            setNameserverNotAvailable();
            if (config.getCompensationErrorCacheEnable()) {
                return nameServiceCacheManager.getBrokerByRetryType(retryType);
            }
            throw new NsrException(e);
        }
    }

    @Override
    public List<Consumer> getConsumerByTopic(TopicName topic) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getConsumerByTopic(topic);
        }
        if (config.getCompensationErrorCacheEnable() && !nameserverIsAvailable()) {
            return nameServiceCacheManager.getConsumerByTopic(topic);
        }
        try {
            List<Consumer> consumerByTopic = delegate.getConsumerByTopic(topic);
            setNameserverAvailable();
            return consumerByTopic;
        } catch (Exception e) {
            logger.error("getConsumerByTopic exception, topic: {}", topic, e);
            setNameserverNotAvailable();
            if (config.getCompensationErrorCacheEnable()) {
                return nameServiceCacheManager.getConsumerByTopic(topic);
            }
            throw new NsrException(e);
        }
    }

    @Override
    public List<Producer> getProducerByTopic(TopicName topic) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getProducerByTopic(topic);
        }
        if (config.getCompensationErrorCacheEnable() && !nameserverIsAvailable()) {
            return nameServiceCacheManager.getProducerByTopic(topic);
        }
        try {
            List<Producer> producerByTopic = delegate.getProducerByTopic(topic);
            setNameserverAvailable();
            return producerByTopic;
        } catch (Exception e) {
            logger.error("getProducerByTopic exception, topic: {}", topic, e);
            setNameserverNotAvailable();
            if (config.getCompensationErrorCacheEnable()) {
                return nameServiceCacheManager.getProducerByTopic(topic);
            }
            throw new NsrException(e);
        }
    }

    @Override
    public List<Replica> getReplicaByBroker(Integer brokerId) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getReplicaByBroker(brokerId);
        }
        if (config.getCompensationErrorCacheEnable() && !nameserverIsAvailable()) {
            return nameServiceCacheManager.getReplicaByBroker(brokerId);
        }
        try {
            List<Replica> replicaByBroker = delegate.getReplicaByBroker(brokerId);
            setNameserverAvailable();
            return replicaByBroker;
        } catch (Exception e) {
            logger.error("getReplicaByBroker exception, brokerId: {}", brokerId, e);
            setNameserverNotAvailable();
            if (config.getCompensationErrorCacheEnable()) {
                return nameServiceCacheManager.getReplicaByBroker(brokerId);
            }
            throw new NsrException(e);
        }
    }

    @Override
    public AppToken getAppToken(String app, String token) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getAppToken(app, token);
        }
        if (config.getCompensationErrorCacheEnable() && !nameserverIsAvailable()) {
            return nameServiceCacheManager.getAppToken(app, token);
        }
        try {
            AppToken appToken = delegate.getAppToken(app, token);
            setNameserverAvailable();
            return appToken;
        } catch (Exception e) {
            logger.error("getAppToken exception, app: {}, token: {}", app, token, e);
            setNameserverNotAvailable();
            if (config.getCompensationErrorCacheEnable()) {
                return nameServiceCacheManager.getAppToken(app, token);
            }
            throw new NsrException(e);
        }
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

    protected boolean nameserverIsAvailable() {
        return nameserverLastAvailableTime == 0
                || config.getCompensationErrorThreshold() > nameserverNotAvailableCounter.get()
                || SystemClock.now() - nameserverLastAvailableTime > config.getCompensationErrorRetryInterval();
    }

    protected void setNameserverNotAvailable() {
        if (nameserverLastAvailableTime == 0) {
            nameserverLastAvailableTime = SystemClock.now();
        }
        nameserverNotAvailableCounter.incrementAndGet();
    }

    protected void setNameserverAvailable() {
        nameserverLastAvailableTime = 0;
        nameserverNotAvailableCounter.set(0);
    }

    public NameService getDelegate() {
        return delegate;
    }
}