/**
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
package com.jd.joyqueue.nsr;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.jd.joyqueue.domain.AppToken;
import com.jd.joyqueue.domain.Broker;
import com.jd.joyqueue.domain.ClientType;
import com.jd.joyqueue.domain.Config;
import com.jd.joyqueue.domain.Consumer;
import com.jd.joyqueue.domain.DataCenter;
import com.jd.joyqueue.domain.PartitionGroup;
import com.jd.joyqueue.domain.Producer;
import com.jd.joyqueue.domain.Replica;
import com.jd.joyqueue.domain.Subscription;
import com.jd.joyqueue.domain.Topic;
import com.jd.joyqueue.domain.TopicConfig;
import com.jd.joyqueue.domain.TopicName;
import com.jd.joyqueue.event.BrokerEvent;
import com.jd.joyqueue.event.ConfigEvent;
import com.jd.joyqueue.event.ConsumerEvent;
import com.jd.joyqueue.event.DataCenterEvent;
import com.jd.joyqueue.event.MetaEvent;
import com.jd.joyqueue.event.NameServerEvent;
import com.jd.joyqueue.event.PartitionGroupEvent;
import com.jd.joyqueue.event.ProducerEvent;
import com.jd.joyqueue.event.TopicEvent;
import com.jd.joyqueue.network.transport.TransportServer;
import com.jd.joyqueue.network.transport.config.ServerConfig;
import com.jd.joyqueue.nsr.config.NameServerConfig;
import com.jd.joyqueue.nsr.message.MessageListener;
import com.jd.joyqueue.nsr.message.Messenger;
import com.jd.joyqueue.nsr.network.NsrTransportServerFactory;
import com.jd.joyqueue.nsr.service.AppTokenService;
import com.jd.joyqueue.nsr.service.BrokerService;
import com.jd.joyqueue.nsr.service.ConfigService;
import com.jd.joyqueue.nsr.service.ConsumerService;
import com.jd.joyqueue.nsr.service.DataCenterService;
import com.jd.joyqueue.nsr.service.NamespaceService;
import com.jd.joyqueue.nsr.service.PartitionGroupReplicaService;
import com.jd.joyqueue.nsr.service.PartitionGroupService;
import com.jd.joyqueue.nsr.service.ProducerService;
import com.jd.joyqueue.nsr.service.TopicService;
import com.jd.joyqueue.nsr.util.DCWrapper;
import com.jd.joyqueue.toolkit.concurrent.EventBus;
import com.jd.joyqueue.toolkit.concurrent.EventListener;
import com.jd.joyqueue.toolkit.config.PropertySupplier;
import com.jd.joyqueue.toolkit.config.PropertySupplierAware;
import com.jd.joyqueue.toolkit.lang.Close;
import com.jd.joyqueue.toolkit.lang.LifeCycle;
import com.jd.joyqueue.toolkit.service.Service;
import com.jd.joyqueue.toolkit.time.SystemClock;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import com.jd.laf.extension.Type;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jd.joyqueue.event.NameServerEvent.BROKER_ID_ALL_BROKER;

/**
 * 1.启动nameServer
 * 2.器动管理端的http接口
 *
 * @author wylixiaobin
 * @date 2018/9/4
 */
public class NameServer extends Service implements NameService, PropertySupplierAware, Type {
    /**
     * name server config
     */
    private NameServerConfig nameServerConfig;
    /**
     * transport factory
     */
    private NsrTransportServerFactory transportServerFactory;
    /**
     * transport server
     */
    private TransportServer transportServer;
    /**
     * manage server
     */
    private ManageServer manageServer;
    /**
     * 元数据管理器
     */
    private MetaManager metaManager;
    /**
     * 元数据变更监听器
     */
    private MessageListener listener;
    /**
     * properties
     */
    private PropertySupplier propertySupplier;
    /**
     * service provider
     */
    private ServiceProvider serviceProvider;
    /**
     * meta data cache
     */
    private MetaCache metaCache = new MetaCache();

    /**
     * 事件管理器
     */
    protected EventBus<NameServerEvent> eventManager = new EventBus<>("BROKER_NAMESERVER_EVENT_BUS");
    /**
     * service provider
     */
    public static ExtensionPoint<ServiceProvider, String> serviceProviderPoint = new ExtensionPointLazy<>(ServiceProvider.class);
    private static final Logger logger = LoggerFactory.getLogger(NameServer.class);

    private Cache<String, Map<TopicName, TopicConfig>> appTopicCache;

    public NameServer() {
        // do nothing
    }

    @Override
    protected void validate() throws Exception {
        super.validate();
        if (nameServerConfig == null) {
            nameServerConfig = new NameServerConfig(propertySupplier);
        }
        if (serviceProvider == null){
            serviceProvider = loadServiceProvider(propertySupplier);
        }
        if (metaManager == null) {
            metaManager = buildMetaManager();
        }
        if (listener == null) {
            listener = new MetaDataListener();
        }
        if (transportServerFactory == null) {
            this.transportServerFactory = new NsrTransportServerFactory(this);
        }
        if (manageServer == null) {
            this.manageServer = buildManageServer();
        }
        if (transportServer == null){
            this.transportServer = buildTransportServer();
        }
        if (appTopicCache == null) {
            this.appTopicCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(nameServerConfig.getCacheExpireTime(), TimeUnit.MILLISECONDS)
                    .build();
        }
    }



    @Override
    public void doStart() throws Exception {
        super.doStart();
        this.metaManager.addListener(listener);
        this.manageServer.setManagerPort(nameServerConfig.getManagerPort());

        this.metaManager.start();
        this.eventManager.start();
        this.transportServer.start();
        this.manageServer.start();
        logger.info("nameServer is started");
    }

    @Override
    public void doStop() {
        super.doStop();
        try {
            Close.close(manageServer);
            Close.close(metaManager);
            Close.close(eventManager);
            Close.close(transportServer);
            if(serviceProvider instanceof LifeCycle)
            Close.close((LifeCycle )serviceProvider);
        } finally {
            logger.info("nameServer is stopped");
        }
    }

    @Override
    public List<TopicConfig> subscribe(List<Subscription> subscriptions, ClientType clientType) {
        List<TopicConfig> configs = new ArrayList<>();
        subscriptions.forEach(subscription -> {
            TopicConfig topicConfig = subscribe(subscription, clientType);
            if (null != topicConfig) {
                configs.add(topicConfig);
            }
        });
        return configs;
    }

    @Override
    public TopicConfig subscribe(Subscription subscription, ClientType clientType) {
        //TODO 考虑下这个
        if (subscription.getType() == Subscription.Type.CONSUMPTION) {
            TopicName topic = subscription.getTopic();
            String app = subscription.getApp();
            TopicConfig topicConfig = getTopicConfig(topic);
            if (null == topicConfig){
                return null;
            }
            Map<String, Consumer> consumerConfigMap = metaCache.consumerConfigs.get(topic);
            if (null == consumerConfigMap){
                consumerConfigMap = new ConcurrentHashMap<>();
            }
            if (!consumerConfigMap.containsKey(app)) {
                Consumer consumer = metaManager.getConsumer(topic, app);
                if (null == consumer) {
                    consumer = new Consumer();
                    consumer.setTopic(topic);
                    consumer.setApp(app);

                    consumer.setClientType(clientType);
                    consumer = metaManager.addConsumer(consumer);
                    consumerConfigMap.put(app, consumer);
                }
            }
            return topicConfig;
        } else if (subscription.getType() == Subscription.Type.PRODUCTION) {
            TopicName topic = subscription.getTopic();
            String app = subscription.getApp();
            TopicConfig topicConfig = getTopicConfig(topic);
            if (null == topicConfig) return null;
            Map<String, Producer> producerConfigMap = metaCache.producerConfigs.get(topic);
            if (null == producerConfigMap) producerConfigMap = new ConcurrentHashMap<>();
            if (!producerConfigMap.containsKey(app)) {
                Producer producer = metaManager.getProducer(topic, app);
                if (null == producer) {
                    producer = new Producer();
                    producer.setTopic(topic);
                    producer.setApp(app);

                    producer.setClientType(clientType);
                    producer = metaManager.addProducer(producer);
                    producerConfigMap.put(app, producer);
                }
            }
            return topicConfig;
        } else {
            throw new IllegalStateException("operation do not supported");
        }
    }

    @Override
    public void unSubscribe(Subscription subscription) {
        if (subscription.getType() == Subscription.Type.CONSUMPTION) {
            TopicConfig topicConfig = getTopicConfig(subscription.getTopic());
            if (null == topicConfig){
                return;
            }
            Map<String, Consumer> consumerConfigMap = metaCache.consumerConfigs.get(subscription.getTopic());
            //todo 取消订阅
            consumerConfigMap.remove(subscription.getApp());
            metaManager.removeConsumer(subscription.getTopic(), subscription.getApp());
        } else if (subscription.getType() == Subscription.Type.PRODUCTION) {
            TopicConfig topicConfig = getTopicConfig(subscription.getTopic());
            if (null == topicConfig) return;
            Map<String, Producer> producerConfigMap = metaCache.producerConfigs.get(subscription.getTopic());
            //todo 取消订阅
            producerConfigMap.remove(subscription.getApp());
            metaManager.removeProducer(subscription.getTopic(), subscription.getApp());
        } else {
            throw new IllegalStateException("operation do not supported");
        }

    }

    @Override
    public void unSubscribe(List<Subscription> subscriptions) {
        subscriptions.forEach(subscription -> unSubscribe(subscription));
    }


    @Override
    public void leaderReport(TopicName topic, int partitionGroup, int leaderBrokerId, Set<Integer> isrId, int termId) {
        logger.info("Leader report, topic is {}, partition group is {}, leader is {}, term is {}",
                topic, partitionGroup, leaderBrokerId, termId);
        TopicConfig topicConfig = null;
        try {
            topicConfig = reloadTopicConfig(topic);
        } catch (Exception e) {
            logger.warn("try to reload topic config failure, topic[{}]", topic.getFullName(), e);
        }
        if (topicConfig == null) {
            topicConfig = metaCache.topicConfigs.get(topic);
        }
        if (topicConfig == null) {
            return;
        }
        PartitionGroup group = null;
        Iterator<PartitionGroup> groupIterator = topicConfig.getPartitionGroups().values().iterator();
        while (groupIterator.hasNext()) {
            PartitionGroup pgroup = groupIterator.next();
            if (pgroup.getGroup() == partitionGroup) {
                if (pgroup.getTerm() > termId || (pgroup.getTerm() == termId && leaderBrokerId == -1)) {
                    logger.warn("Leader report for topic {} group {}, term {} less than current term {}, leaderId is {}",
                            topic, partitionGroup, termId, pgroup.getTerm(), leaderBrokerId);
                    return;
                }
                group = pgroup;
                break;
            }
        }
        if (null == group) {
            throw new RuntimeException(String.format("topic[%s] group[%s] is not exist", topic, partitionGroup));
        }
        group.setIsrs(isrId);
        group.setLeader(leaderBrokerId);
        group.setTerm(termId);
        metaManager.updatePartitionGroup(group);
    }

    @Override
    public Broker getBroker(int brokerId) {
        Broker broker = metaCache.brokerConfigs.get(brokerId);
        if (null != broker) {
            return broker;
        }
        return reloadBroker(brokerId, false);
    }

    @Override
    public List<Broker> getAllBrokers() {
        List<Broker> brokers = null;
        try {
            brokers = reloadBrokers();
        } catch (Exception ignored) {
        }

        if (brokers == null) {
            Collection col = metaCache.brokerConfigs.values();
            brokers = (CollectionUtils.isEmpty(col) ? Collections.emptyList() : new ArrayList<>(col));
        }

        return brokers;
    }

    @Override
    public void addTopic(Topic topic, List<PartitionGroup> partitionGroups) {
        metaManager.addTopic(topic, partitionGroups);
    }

    @Override
    public TopicConfig getTopicConfig(TopicName topic) {
        TopicConfig topicConfig = metaCache.topicConfigs.get(topic);
        if (null != topicConfig) {
            return topicConfig;
        } else {
            return reloadTopicConfig(topic);
        }
    }

    @Override
    public Set<String> getAllTopics() {
        List<Topic> allTopics = metaManager.getAllTopics();
        if (null != allTopics) {
            Set<String> topics = new HashSet<>();
            allTopics.forEach(topic -> topics.add(topic.getName().getFullName()));
            return topics;
        } else {
            return Collections.emptySet();
        }
    }


    //TODO Ignite不可用
    @Override
    public Set<String> getTopics(String app, Subscription.Type subscribe) {
        Set<String> topics = new HashSet<>();
        if (null == subscribe) {
            List<Producer> producers = metaManager.getProducer(app);
            if (null != producers){
                producers.forEach(producer -> topics.add(producer.getTopic().getFullName()));
            }
            List<Consumer> consumers = metaManager.getConsumer(app);
            if (null != consumers){
                consumers.forEach(consumer -> topics.add(consumer.getTopic().getFullName()));
            }
        } else {
            switch (subscribe) {
                case PRODUCTION:
                    List<Producer> producers = metaManager.getProducer(app);
                    if (null != producers){
                        producers.forEach(producer -> topics.add(producer.getTopic().getFullName()));
                    }
                    break;
                case CONSUMPTION:
                    List<Consumer> consumers = metaManager.getConsumer(app);
                    if (null != consumers){
                        consumers.forEach(consumer -> topics.add(consumer.getTopic().getFullName()));
                    }
                    break;
            }
        }
        return topics;
    }

    @Override
    public Map<TopicName, TopicConfig> getTopicConfigByBroker(Integer brokerId) {
        Map<TopicName,TopicConfig> map = new HashMap<>();
        List<Replica> replicas = metaManager.getReplicaByBroker(brokerId);
        for(Replica replica:replicas){
            if(!map.containsKey(replica.getTopic())){
                map.put(replica.getTopic(),getTopicConfig(replica.getTopic()));
            }
        }
        return map;
    }

    @Override
    public List<Replica> getReplicaByBroker(Integer brokerId) {
        return metaManager.getReplicaByBroker(brokerId);
    }

    @Override
    public AppToken getAppToken(String app, String token) {
        AppToken appToken = reloadAppToken(app, token);
        if (appToken == null) {
            reloadAppToken();
            appToken =  metaCache.appTokens.get(createAppTokenCacheKey(app, token));
        }

        return appToken;
    }




    @Override
    public Broker register(Integer brokerId, String brokerIp, Integer port) {
        Broker broker = null;
        if (null == brokerId) {
            broker = metaManager.getBrokerByIpAndPort(brokerIp, port);
            if (null == broker) {
                brokerId = Integer.parseInt(String.valueOf(SystemClock.now() / 1000));
                broker = new Broker();
                broker.setId(brokerId);
                broker.setIp(brokerIp);
                DataCenter dataCenter = getDataCenter(brokerIp);
                broker.setDataCenter(dataCenter.getCode());
                broker.setPort(port);
                broker.setRetryType(Broker.DEFAULT_RETRY_TYPE);
                broker.setPermission(Broker.PermissionEnum.FULL);
                metaManager.addBroker(broker);
                logger.info("register broker success broker.id {}", brokerId);
            } else {
                brokerId = broker.getId();
            }
        }
        if (null != broker) {
            metaCache.brokerConfigs.put(brokerId, broker);
        }
        //TODO 并未去更新broker的IP
        broker = reloadBroker(brokerId, true);
        if (null != broker) {
            broker.setIp(brokerIp);
            broker.setPort(port);
            broker.setDataCenter(getDataCenter(brokerIp).getCode());
            metaManager.addBroker(broker);
        }

        return broker;
    }

    @Override
    public Producer getProducerByTopicAndApp(TopicName topic, String app) {
        Map<String, Producer> cachedAppProducers = metaCache.producerConfigs.get(topic);
        if (cachedAppProducers != null && cachedAppProducers.containsKey(app)) {
            return cachedAppProducers.get(app);
        } else {
            Producer producer = metaManager.getProducer(topic, app);
            if (null != producer) {
                if (cachedAppProducers == null) {
                    cachedAppProducers = new ConcurrentHashMap<>();
                    Map<String, Producer> preCache = metaCache.producerConfigs.putIfAbsent(topic, cachedAppProducers);
                    if (preCache != null) {
                        cachedAppProducers = preCache;
                    }
                }
                cachedAppProducers.put(app, producer);
            }
            return producer;
        }
    }

    @Override
    public Consumer getConsumerByTopicAndApp(TopicName topic, String app) {
        Map<String, Consumer> cachedAppConsumers = metaCache.consumerConfigs.get(topic);
        if (cachedAppConsumers != null && cachedAppConsumers.containsKey(app)) {
            return cachedAppConsumers.get(app);
        } else {
            Consumer consumer = metaManager.getConsumer(topic, app);
            if (null != consumer) {
                if (cachedAppConsumers == null) {
                    cachedAppConsumers = new ConcurrentHashMap();
                }

                Map<String, Consumer> preCached = metaCache.consumerConfigs.putIfAbsent(topic, cachedAppConsumers);
                if (preCached != null) {
                    cachedAppConsumers = preCached;
                }
                cachedAppConsumers.put(app, consumer);
            }
            return consumer;
        }
    }

    @Override
    public Map<TopicName, TopicConfig> getTopicConfigByApp(String subscribeApp, Subscription.Type subscribe) {
        if (nameServerConfig.getCacheEnable()) {
            try {
                return appTopicCache.get(subscribeApp + "_" + String.valueOf(subscribe), new Callable<Map<TopicName, TopicConfig>>() {
                    @Override
                    public Map<TopicName, TopicConfig> call() throws Exception {
                        return doGetTopicConfigByApp(subscribeApp, subscribe);
                    }
                });
            } catch (ExecutionException e) {
                logger.error("getTopicConfigByApp exception, subscribeApp: {}, subscribe: {}",
                        subscribeApp, subscribe);
                return Maps.newHashMap();
            }
        } else {
            return doGetTopicConfigByApp(subscribeApp, subscribe);
        }
    }

    protected Map<TopicName, TopicConfig> doGetTopicConfigByApp(String subscribeApp, Subscription.Type subscribe) {
        Map<TopicName, TopicConfig> appTopicConfigs = new HashMap<>();

        List<? extends Subscription> subscriptions = null;
        switch (subscribe) {
            case CONSUMPTION:
                subscriptions = metaManager.getConsumer(subscribeApp);
                break;
            case PRODUCTION:
                subscriptions = metaManager.getProducer(subscribeApp);
                break;
        }

        if (null != subscriptions) {
            subscriptions.forEach(p -> {
                TopicConfig topicConfig = getTopicConfig(p.getTopic());
                if (null != topicConfig) {
                    appTopicConfigs.put(p.getTopic(), topicConfig);
                }
            });
        }
        return appTopicConfigs;
    }

    @Override
    public boolean hasSubscribe(String subscribeApp, Subscription.Type subscribe) {
        //TODO Ignite 不可用
        switch (subscribe) {
            case CONSUMPTION:
                List<Consumer> consumers = metaManager.getConsumer(subscribeApp);
                return (null != consumers && consumers.size() > 0) ? true : false;
            case PRODUCTION:
                List<Producer> producers = metaManager.getProducer(subscribeApp);
                return (null != producers && producers.size() > 0) ? true : false;
        }
        return false;
    }

    @Override
    public DataCenter getDataCenter(String ip) {
        if (metaCache.dataCenterMap.isEmpty()) {
            Collection<DataCenter> dcs = metaManager.getAllDataCenter();
            //应该不需要同步
            // synchronized (metaCache.dataCenterMap) {
            if (null != dcs) {
                dcs.forEach(dataCenter -> {
                    metaCache.dataCenterMap.put(dataCenter.getCode(), new DCWrapper(dataCenter));
                });
            }
            //}
        }
        Optional<DCWrapper> optional = metaCache.dataCenterMap.values().stream().filter(dataCenter -> dataCenter.match(ip)).findFirst();
        if (optional.isPresent()) {
            return optional.get().getDataCenter();
        }
        return DataCenter.DEFAULT;
    }

    @Override
    public String getConfig(String group, String key) {
        Config config = metaManager.getConfig(group, key);
        return config == null ? null : config.getValue();
    }

    @Override
    public List<Config> getAllConfigs() {
        return metaManager.getAllConfigs();
    }

    @Override
    public List<Broker> getBrokerByRetryType(String retryType) {
        return metaManager.getBrokerByRetryType(retryType);
    }

    @Override
    public List<Consumer> getConsumerByTopic(TopicName topic) {
        return metaManager.getConsumerByTopic(topic);
    }

    @Override
    public List<Producer> getProducerByTopic(TopicName topic) {
        return metaManager.getProducerByTopic(topic);
    }

    @Override
    public void addListener(EventListener<NameServerEvent> listener) {
        //TODO 是否需要全量更新一下
        eventManager.addListener(listener);
    }

    @Override
    public void removeListener(EventListener<NameServerEvent> listener) {
        eventManager.removeListener(listener);
    }

    @Override
    public void addEvent(NameServerEvent event) {
        eventManager.add(event);
    }

    private TopicConfig reloadTopicConfig(TopicName topicCode) {
        Topic topic = metaManager.getTopicByName(topicCode);
        if (null == topic) {
            return null;
        }
        List<PartitionGroup> partitionGroups = metaManager.getPartitionGroupByTopic(topicCode);
        TopicConfig old = metaCache.topicConfigs.get(topicCode);
        TopicConfig topicConfig = TopicConfig.toTopicConfig(topic);
        topicConfig.setPartitionGroups(partitionGroups.stream().collect(Collectors.toMap(PartitionGroup::getGroup, group -> group)));
        metaCache.topicConfigs.put(topicCode, topicConfig);
        if (null != partitionGroups) {
            partitionGroups.forEach(group -> {
                Map<Integer, Broker> brokerMap = new HashMap<>();
                group.getReplicas().forEach(brokerId -> {
                    if (!brokerMap.containsKey(brokerId)) {
                        brokerMap.put(brokerId, reloadBroker(brokerId, false));
                    }
                });
                group.setBrokers(brokerMap);
            });
        }

        if (null != old) {
            Set<Integer> removeBrokerIds = old.fetchAllBrokerIds();
            removeBrokerIds.removeAll(topicConfig.fetchAllBrokerIds());
        }
        /**
         * 初始化 consumerConfigs
         */
        if (!metaCache.consumerConfigs.containsKey(topicCode)) {
            metaCache.consumerConfigs.put(topicCode, new HashMap<>());
        }
        if (!metaCache.producerConfigs.containsKey(topicCode)) {
            metaCache.producerConfigs.put(topicCode, new HashMap<>());
        }
        return topicConfig;
    }

    private Consumer reloadConsumer(TopicName topic, String app) {
        Consumer consumer = metaManager.getConsumer(topic, app);
        if (null != consumer) {
            Map<String, Consumer> cachedConsumers = metaCache.consumerConfigs.get(topic);
            if (cachedConsumers == null) {
                cachedConsumers = new HashMap<>();
                Map<String, Consumer> preCache = metaCache.consumerConfigs.putIfAbsent(topic, cachedConsumers);
                if (preCache != null) {
                    cachedConsumers = preCache;
                }
            }
            cachedConsumers.put(app, consumer);
        }
        return consumer;
    }

    private Producer reloadProducer(TopicName topic, String app) {
        Producer producer = metaManager.getProducer(topic, app);
        if (null != producer) {
            Map<String, Producer> cachedProducers = metaCache.producerConfigs.get(topic);
            if (cachedProducers == null) {
                cachedProducers = new HashMap<>();
                Map preCache = metaCache.producerConfigs.putIfAbsent(topic, cachedProducers);
                if (preCache != null) {
                    cachedProducers = preCache;
                }
            }
            cachedProducers.put(app, producer);
        }
        return producer;
    }

    private Broker reloadBroker(Integer brokerId, boolean reLoadTopic) {
        Broker broker = metaManager.getBrokerById(brokerId);
        if (null == broker) {
            return null;
        }
        metaCache.brokerConfigs.put(broker.getId(), broker);
        if (reLoadTopic) {
            Set<TopicName> topics = metaManager.getTopicByBroker(brokerId);
            if (null != topics){
                topics.forEach(topic -> {
                    reloadTopicConfig(topic);
                });
            }
        }
        return broker;
    }

    private List<Broker> reloadBrokers() {
        List<Broker> brokers = metaManager.getAllBrokers();
        for (Broker broker : brokers) {
            metaCache.brokerConfigs.put(broker.getId(), broker);
        }
        return brokers;
    }

    @Override
    public String type() {
        return "server";
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.propertySupplier = supplier;
    }

    /**
     * MetaDataListener
     */
    protected class MetaDataListener implements MessageListener<MetaEvent> {

        @Override
        public void onEvent(MetaEvent event) {
            logger.info("event[{}]", event);
            switch (event.getEventType()) {
                case ADD_TOPIC:
                case UPDATE_TOPIC:
                    TopicName topic1 = ((TopicEvent) event).getTopic();
                    TopicConfig topicConfig1 = reloadTopicConfig(topic1);
                    logger.info("UPDATE_TOPIC [{}]]", topicConfig1);
                    if (null != topicConfig1) {
                        topicConfig1.fetchAllBrokerIds().forEach(brokerId -> {
                                eventManager.add(new NameServerEvent(event, brokerId));
                        });
                    }
                    break;
                case REMOVE_TOPIC:
                    TopicName topic2 = ((TopicEvent) event).getTopic();
                    TopicConfig topicConfig2 = metaCache.topicConfigs.remove(topic2);
                    logger.info("REMOVE_TOPIC [{}]]", topic2);
                    if (null != topicConfig2) {
                        topicConfig2.fetchAllBrokerIds().forEach(brokerId -> {
                                eventManager.add(new NameServerEvent(event, brokerId));
                        });
                    }
                    break;
                case ADD_PARTITION_GROUP:
                case UPDATE_PARTITION_GROUP:
                    TopicName topic3 = ((PartitionGroupEvent) event).getTopic();
                    TopicConfig topicConfig3Old = getTopicConfig(topic3);
                    TopicConfig topicConfig3 = reloadTopicConfig(topic3);
                    logger.info("UPDATE_PARTITION_GROUP [{}]]", topicConfig3);
                    Set<Integer> broker3 = new HashSet<>();
                    if (null != topicConfig3){
                        broker3.addAll(topicConfig3.fetchAllBrokerIds());
                    }
                    if (null != topicConfig3Old){
                        broker3.addAll(topicConfig3Old.fetchAllBrokerIds());
                    }
                    broker3.forEach(brokerId -> {
                        eventManager.add(new NameServerEvent(event, brokerId));
                    });
                    break;
                case REMOVE_PARTITION_GROUP:
                    TopicName topic4 = ((PartitionGroupEvent) event).getTopic();
                    TopicConfig topicConfig4Old = getTopicConfig(topic4);
                    TopicConfig topicConfig4 = reloadTopicConfig(topic4);
                    logger.info("REMOVE_PARTITION_GROUP [{}]]", topicConfig4);
                    if (null != topicConfig4) {
                        (null == topicConfig4Old ? topicConfig4 : topicConfig4Old).fetchAllBrokerIds().forEach(brokerId -> {
                                eventManager.add(new NameServerEvent(event, brokerId));
                        });
                    }
                    break;
                case ADD_CONSUMER:
                case UPDATE_CONSUMER:
                    ConsumerEvent consumerEvent = (ConsumerEvent) event;
                    Consumer consumer = reloadConsumer(consumerEvent.getTopic(), consumerEvent.getApp());
                    logger.info("UPDATE_CONSUMER [{}],brokers[{}]", consumer, metaCache.brokerConfigs);
                    if (metaCache.topicConfigs.containsKey(consumerEvent.getTopic())) {
                        metaCache.topicConfigs.get(consumerEvent.getTopic()).fetchAllBrokerIds().forEach(brokerId -> {
                                eventManager.add(new NameServerEvent(event, brokerId));
                        });
                    }
                    break;
                case REMOVE_CONSUMER:
                    ConsumerEvent removeConsumer = (ConsumerEvent) event;
                    TopicName topic5 = removeConsumer.getTopic();
                    TopicConfig topicConfig5 = metaCache.topicConfigs.get(topic5);
                    logger.info("REMOVE_CONSUMER [{}],brokers[{}]", removeConsumer, metaCache.brokerConfigs);
                    if (metaCache.consumerConfigs.containsKey(topic5)) {
                        metaCache.consumerConfigs.get(topic5).remove(removeConsumer.getApp());
                    }
                    if (null != topicConfig5) {
                        topicConfig5.fetchAllBrokerIds().forEach(brokerId -> {
                                eventManager.add(new NameServerEvent(event, brokerId));
                        });
                    }
                    break;
                case ADD_PRODUCER:
                case UPDATE_PRODUCER:
                    ProducerEvent producerEvent = (ProducerEvent) event;
                    Producer producer = reloadProducer(producerEvent.getTopic(), producerEvent.getApp());
                    logger.info("UPDATE_PRODUCER [{}],brokers[{}]", producer, metaCache.brokerConfigs);
                    if (metaCache.topicConfigs.containsKey(producerEvent.getTopic())) {
                        metaCache.topicConfigs.get(producerEvent.getTopic()).fetchAllBrokerIds().forEach(brokerId -> {
                                eventManager.add(new NameServerEvent(event, brokerId));
                        });
                    }
                    ;
                    break;
                case REMOVE_PRODUCER:
                    ProducerEvent removeProducer = (ProducerEvent) event;
                    TopicConfig topicConfig7 = metaCache.topicConfigs.get(removeProducer.getTopic());
                    logger.info("REMOVE_PRODUCER [{}],brokers[{}]", removeProducer, metaCache.brokerConfigs);
                    if (metaCache.producerConfigs.containsKey(removeProducer.getTopic())) {
                        metaCache.producerConfigs.get(removeProducer.getTopic()).remove(removeProducer.getApp());
                    }
                    if (null != topicConfig7) {
                        topicConfig7.fetchAllBrokerIds().forEach(brokerId -> {
                                eventManager.add(new NameServerEvent(event, brokerId));
                        });
                    }
                    break;
                case ADD_DATACENTER:
                case UPDATE_DATACENTER:
                    DataCenterEvent dcEvent = (DataCenterEvent) event;
                    logger.info("UPDATE_DATACENTER [{}]", dcEvent);
                    String id = dcEvent.getRegion() + "_" + dcEvent.getCode();
                    metaCache.dataCenterMap.put(dcEvent.getCode(), new DCWrapper(metaManager.getDataCenter(id)));
                    break;
                case REMOVE_DATACENTER:
                    if (logger.isDebugEnabled()) {
                        logger.debug("REMOVE_DATACENTER [{}]", event);
                    }
                    metaCache.dataCenterMap.remove(((ConfigEvent) event).getKey());
                    break;
                case ADD_CONFIG:
                case UPDATE_CONFIG:
                    ConfigEvent configEvent = (ConfigEvent) event;
                    logger.info("UPDATE_CONFIG [{}]", configEvent);
                    eventManager.add(new NameServerEvent(event, BROKER_ID_ALL_BROKER));
                    break;
                case REMOVE_CONFIG:
                    logger.info("REMOVE_DATACENTER [{}]", event);
                    eventManager.add(new NameServerEvent(event, BROKER_ID_ALL_BROKER));
                    break;
                case UPDATE_BROKER:
                    logger.info("UPDATE_BROKER [{}]", event);
                    Integer brokerId = ((BrokerEvent)event).getBroker().getId();
                    reloadBroker(brokerId,true);
                    eventManager.add(new NameServerEvent(event, brokerId));
                    break;
                default:
                    break;
            }
        }
    }

    protected class MetaCache {
        /**
         * app tokens
         */
        private Map<String, AppToken> appTokens = new ConcurrentHashMap<>();
        /**
         * 数据中心
         */
        private Map<String, DCWrapper> dataCenterMap = new ConcurrentHashMap<>();
        /**
         * broker配置
         * Map<datacenter@ip@port,BrokerConfig>
         */
        private Map<Integer, Broker> brokerConfigs = new ConcurrentHashMap<>();
        /**
         * 主题配置
         */
        private Map<TopicName, TopicConfig> topicConfigs = new ConcurrentHashMap<>();
        /**
         * 消费配置
         */
        private Map<TopicName, Map<String, Consumer>> consumerConfigs = new ConcurrentHashMap<>();
        /**
         * 发送配置
         */
        private Map<TopicName, Map<String, Producer>> producerConfigs = new ConcurrentHashMap<>();

    }

    private ManageServer buildManageServer() {
        TopicService topicService = serviceProvider.getService(TopicService.class);
        BrokerService brokerService = serviceProvider.getService(BrokerService.class);
        ConsumerService consumerService = serviceProvider.getService(ConsumerService.class);
        ProducerService producerService = serviceProvider.getService(ProducerService.class);
        ConfigService configService = serviceProvider.getService(ConfigService.class);
        AppTokenService appTokenService = serviceProvider.getService(AppTokenService.class);
        DataCenterService dataCenterService = serviceProvider.getService(DataCenterService.class);
        NamespaceService namespaceService = serviceProvider.getService(NamespaceService.class);
        PartitionGroupService partitionGroupService = serviceProvider.getService(PartitionGroupService.class);
        PartitionGroupReplicaService partitionGroupReplicaService = serviceProvider.getService(PartitionGroupReplicaService.class);

        Preconditions.checkArgument(brokerService != null, "broker service can not be null");
        Preconditions.checkArgument(topicService != null, "topic service can not be null");
        Preconditions.checkArgument(consumerService != null, "consumer service can not be null");
        Preconditions.checkArgument(producerService != null, "producer service can not be null");
        Preconditions.checkArgument(appTokenService != null, "appToken service can not be null");
        Preconditions.checkArgument(namespaceService != null, "namespace service can not be null");
        Preconditions.checkArgument(dataCenterService != null, "datacenter service can not be null");
        Preconditions.checkArgument(partitionGroupReplicaService != null, "replica service can not be null");
        Preconditions.checkArgument(partitionGroupService != null, "partitionGroup service can not be null");
        return new ManageServer(topicService, producerService, consumerService,
                brokerService, configService, appTokenService, dataCenterService,
                namespaceService, partitionGroupService, partitionGroupReplicaService);

    }


    private MetaManager buildMetaManager() {
        Messenger messenger = serviceProvider.getService(Messenger.class);
        TopicService topicService = serviceProvider.getService(TopicService.class);
        ConfigService configService = serviceProvider.getService(ConfigService.class);
        BrokerService brokerService = serviceProvider.getService(BrokerService.class);
        ConsumerService consumerService = serviceProvider.getService(ConsumerService.class);
        ProducerService producerService = serviceProvider.getService(ProducerService.class);
        AppTokenService appTokenService = serviceProvider.getService(AppTokenService.class);
        DataCenterService dataCenterService = serviceProvider.getService(DataCenterService.class);
        PartitionGroupService partitionGroupService = serviceProvider.getService(PartitionGroupService.class);
        PartitionGroupReplicaService partitionGroupReplicaService = serviceProvider.getService(PartitionGroupReplicaService.class);

        return new MetaManager(messenger, configService, topicService, brokerService,
                consumerService, producerService, partitionGroupService,
                partitionGroupReplicaService, appTokenService, dataCenterService);

    }



    private TransportServer buildTransportServer(){
        ServerConfig serverConfig = nameServerConfig.getServerConfig();
        serverConfig.setPort(nameServerConfig.getServicePort());
        serverConfig.setAcceptThreadName("joyqueue-nameserver-accept-eventLoop");
        serverConfig.setIoThreadName("joyqueue-nameserver-io-eventLoop");
        return transportServerFactory.bind(serverConfig, serverConfig.getHost(), serverConfig.getPort());
    }


    private ServiceProvider loadServiceProvider(PropertySupplier propertySupplier) throws Exception{
        ServiceProvider serviceProvider = serviceProviderPoint.get();
        Preconditions.checkArgument(serviceProvider != null, "service provider can not be null.");
        if (serviceProvider instanceof PropertySupplierAware) {
            ((PropertySupplierAware) serviceProvider).setSupplier(propertySupplier);
        }

        if (serviceProvider instanceof LifeCycle){
            ((LifeCycle) serviceProvider).start();
        }
        return serviceProvider;
    }


    private AppToken reloadAppToken(String app, String token) {
        AppToken appToken = metaManager.findAppToken(app, token);
        if (appToken != null) {
            metaCache.appTokens.put(createAppTokenCacheKey(app, token), appToken);
        }
        return appToken;
    }


    private void reloadAppToken() {
        List<AppToken> appTokens = metaManager.listAppToken();
        if (!CollectionUtils.isEmpty(appTokens)) {
            for (AppToken appToken : appTokens) {
                metaCache.appTokens.put(createAppTokenCacheKey(appToken.getApp(), appToken.getToken()), appToken);
            }
        }
    }


    private String createAppTokenCacheKey(String app, String token) {
        return app + "@" + token;
    }

}
