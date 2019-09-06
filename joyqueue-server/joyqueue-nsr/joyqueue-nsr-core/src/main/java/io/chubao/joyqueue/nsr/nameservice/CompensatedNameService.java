package io.chubao.joyqueue.nsr.nameservice;

import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import io.chubao.joyqueue.domain.AllMetadata;
import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.ClientType;
import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.domain.Subscription;
import io.chubao.joyqueue.domain.Topic;
import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.NameServerEvent;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.nsr.config.NameServiceConfig;
import io.chubao.joyqueue.nsr.message.Messenger;
import io.chubao.joyqueue.toolkit.concurrent.EventBus;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;
import io.chubao.joyqueue.toolkit.lang.LifeCycle;
import io.chubao.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private NameServiceCacheManager nameServiceCacheManager;
    private NameServiceCompensator nameServiceCompensator;
    private NameServiceCompensateThread nameServiceCompensateThread;
    private int brokerId;

    public CompensatedNameService(NameService delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void validate() throws Exception {
        config = new NameServiceConfig(supplier);
        messenger = serviceProviderPoint.get(config.getMessengerType());
        nameServiceCacheManager = new NameServiceCacheManager(config);
        nameServiceCompensator = new NameServiceCompensator(config, eventBus);
        nameServiceCompensateThread = new NameServiceCompensateThread(config, delegate, nameServiceCacheManager, nameServiceCompensator);
    }

    @Override
    protected void doStart() throws Exception {
        enrichIfNecessary(messenger);
        delegate.start();
        eventBus.start();

        messenger.addListener(new NameServiceCacheEventListener(config, nameServiceCacheManager));
        messenger.addListener(new NameServiceEventListenerAdapter(eventBus));

        nameServiceCacheManager.start();
        nameServiceCompensator.start();
        nameServiceCompensateThread.start();
    }

    @Override
    protected void doStop() {
        nameServiceCompensateThread.stop();
        nameServiceCompensator.stop();
        nameServiceCacheManager.stop();
        delegate.stop();
        messenger.stop();
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.supplier = supplier;
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
        try {
            return delegate.getBroker(brokerId);
        } catch (Exception e) {
            logger.error("gerBroker exception, brokerId: {}", brokerId, e);
            return nameServiceCacheManager.getBroker(brokerId);
        }
    }

    @Override
    public List<Broker> getAllBrokers() {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getAllBrokers();
        }
        try {
            return delegate.getAllBrokers();
        } catch (Exception e) {
            logger.error("getAllBrokers exception", e);
            return nameServiceCacheManager.getAllBrokers();
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
        try {
            return delegate.getTopicConfig(topic);
        } catch (Exception e) {
            logger.error("getTopicConfig exception, topic: {]", topic, e);
            return nameServiceCacheManager.getTopicConfig(topic);
        }
    }

    @Override
    public Set<String> getAllTopicCodes() {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getAllTopicCodes();
        }
        try {
            return delegate.getAllTopicCodes();
        } catch (Exception e) {
            logger.error("getAllTopicCodes exception", e);
            return nameServiceCacheManager.getAllTopicCodes();
        }
    }

    @Override
    public Set<String> getTopics(String app, Subscription.Type subscription) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getTopics(app, subscription);
        }
        try {
            return delegate.getTopics(app, subscription);
        } catch (Exception e) {
            logger.error("getTopics exception, app: {}, subscription: {}", app, subscription, e);
            return nameServiceCacheManager.getTopics(app, subscription);
        }
    }

    @Override
    public Map<TopicName, TopicConfig> getTopicConfigByBroker(Integer brokerId) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getTopicConfigByBroker(brokerId);
        }
        try {
            return delegate.getTopicConfigByBroker(brokerId);
        } catch (Exception e) {
            logger.error("getTopicConfigByBroker exception, brokerId: {}", brokerId, e);
            return nameServiceCacheManager.getTopicConfigByBroker(brokerId);
        }
    }

    @Override
    public Broker register(Integer brokerId, String brokerIp, Integer port) {
        Broker broker = delegate.register(brokerId, brokerIp, port);
        this.brokerId = broker.getId();
        this.nameServiceCompensator.setBrokerId(this.brokerId);
        return broker;
    }

    @Override
    public Producer getProducerByTopicAndApp(TopicName topic, String app) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getProducerByTopicAndApp(topic, app);
        }
        try {
            return delegate.getProducerByTopicAndApp(topic, app);
        } catch (Exception e) {
            logger.error("getProducerByTopicAndApp exception, topic: {}, app: {}", topic, app, e);
            return nameServiceCacheManager.getProducerByTopicAndApp(topic, app);
        }
    }

    @Override
    public Consumer getConsumerByTopicAndApp(TopicName topic, String app) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getConsumerByTopicAndApp(topic, app);
        }
        try {
            return delegate.getConsumerByTopicAndApp(topic, app);
        } catch (Exception e) {
            logger.error("getConsumerByTopicAndApp exception, topic: {}, app: {}", topic, app, e);
            return nameServiceCacheManager.getConsumerByTopicAndApp(topic, app);
        }
    }

    @Override
    public Map<TopicName, TopicConfig> getTopicConfigByApp(String subscribeApp, Subscription.Type subscribe) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getTopicConfigByApp(subscribeApp, subscribe);
        }
        try {
            return delegate.getTopicConfigByApp(subscribeApp, subscribe);
        } catch (Exception e) {
            logger.error("getTopicConfigByApp exception, subscribeApp: {}, subscribe: {}", subscribeApp, subscribe, e);
            return nameServiceCacheManager.getTopicConfigByApp(subscribeApp, subscribe);
        }
    }

    @Override
    public DataCenter getDataCenter(String ip) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getDataCenter(ip);
        }
        try {
            return delegate.getDataCenter(ip);
        } catch (Exception e) {
            logger.error("getDataCenter exception, ip: {}", ip, e);
            return nameServiceCacheManager.getDataCenter(ip);
        }
    }

    @Override
    public String getConfig(String group, String key) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getConfig(group, key);
        }
        try {
            return delegate.getConfig(group, key);
        } catch (Exception e) {
            logger.error("getConfig exception, group: {}, key: {}", group, key, e);
            return nameServiceCacheManager.getConfig(group, key);
        }
    }

    @Override
    public List<Config> getAllConfigs() {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getAllConfigs();
        }
        try {
            return delegate.getAllConfigs();
        } catch (Exception e) {
            logger.error("getAllConfigs exception", e);
            return nameServiceCacheManager.getAllConfigs();
        }
    }

    @Override
    public List<Broker> getBrokerByRetryType(String retryType) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getBrokerByRetryType(retryType);
        }
        try {
            return delegate.getBrokerByRetryType(retryType);
        } catch (Exception e) {
            logger.error("getBrokerByRetryType exception, retryType: {}", retryType, e);
            return nameServiceCacheManager.getBrokerByRetryType(retryType);
        }
    }

    @Override
    public List<Consumer> getConsumerByTopic(TopicName topic) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getConsumerByTopic(topic);
        }
        try {
            return delegate.getConsumerByTopic(topic);
        } catch (Exception e) {
            logger.error("getConsumerByTopic exception, topic: {}", topic, e);
            return nameServiceCacheManager.getConsumerByTopic(topic);
        }
    }

    @Override
    public List<Producer> getProducerByTopic(TopicName topic) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getProducerByTopic(topic);
        }
        try {
            return delegate.getProducerByTopic(topic);
        } catch (Exception e) {
            logger.error("getProducerByTopic exception, topic: {}", topic, e);
            return nameServiceCacheManager.getProducerByTopic(topic);
        }
    }

    @Override
    public List<Replica> getReplicaByBroker(Integer brokerId) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getReplicaByBroker(brokerId);
        }
        try {
            return delegate.getReplicaByBroker(brokerId);
        } catch (Exception e) {
            logger.error("getReplicaByBroker exception, brokerId: {}", brokerId, e);
            return nameServiceCacheManager.getReplicaByBroker(brokerId);
        }
    }

    @Override
    public AppToken getAppToken(String app, String token) {
        if (config.getCompensationCacheEnable()) {
            return nameServiceCacheManager.getAppToken(app, token);
        }
        try {
            return delegate.getAppToken(app, token);
        } catch (Exception e) {
            logger.error("getAppToken exception, app: {}, token: {}", app, token, e);
            return nameServiceCacheManager.getAppToken(app, token);
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

    public NameService getDelegate() {
        return delegate;
    }
}