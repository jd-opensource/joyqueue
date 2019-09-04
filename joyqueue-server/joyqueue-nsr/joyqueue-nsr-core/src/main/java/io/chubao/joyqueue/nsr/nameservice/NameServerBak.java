///**
// * Copyright 2019 The JoyQueue Authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
package io.chubao.joyqueue.nsr.nameservice;
//
//import com.google.common.base.Preconditions;
//import com.google.common.cache.Cache;
//import com.google.common.cache.CacheBuilder;
//import com.google.common.collect.Maps;
//import com.jd.laf.extension.ExtensionPoint;
//import com.jd.laf.extension.ExtensionPointLazy;
//import com.jd.laf.extension.Type;
//import io.chubao.joyqueue.domain.AppToken;
//import io.chubao.joyqueue.domain.Broker;
//import io.chubao.joyqueue.domain.ClientType;
//import io.chubao.joyqueue.domain.Config;
//import io.chubao.joyqueue.domain.Consumer;
//import io.chubao.joyqueue.domain.DataCenter;
//import io.chubao.joyqueue.domain.PartitionGroup;
//import io.chubao.joyqueue.domain.Producer;
//import io.chubao.joyqueue.domain.Replica;
//import io.chubao.joyqueue.domain.Subscription;
//import io.chubao.joyqueue.domain.Topic;
//import io.chubao.joyqueue.domain.TopicConfig;
//import io.chubao.joyqueue.domain.TopicName;
//import io.chubao.joyqueue.event.NameServerEvent;
//import io.chubao.joyqueue.network.transport.TransportServer;
//import io.chubao.joyqueue.network.transport.config.ServerConfig;
//import io.chubao.joyqueue.nsr.ManageServer;
//import io.chubao.joyqueue.nsr.MetaManager;
//import io.chubao.joyqueue.nsr.NameService;
//import io.chubao.joyqueue.nsr.ServiceProvider;
//import io.chubao.joyqueue.nsr.config.NameServerConfig;
//import io.chubao.joyqueue.nsr.network.NsrTransportServerFactory;
//import io.chubao.joyqueue.nsr.service.AppTokenService;
//import io.chubao.joyqueue.nsr.service.BrokerService;
//import io.chubao.joyqueue.nsr.service.ConfigService;
//import io.chubao.joyqueue.nsr.service.ConsumerService;
//import io.chubao.joyqueue.nsr.service.DataCenterService;
//import io.chubao.joyqueue.nsr.service.NamespaceService;
//import io.chubao.joyqueue.nsr.service.PartitionGroupReplicaService;
//import io.chubao.joyqueue.nsr.service.PartitionGroupService;
//import io.chubao.joyqueue.nsr.service.ProducerService;
//import io.chubao.joyqueue.nsr.service.TopicService;
//import io.chubao.joyqueue.nsr.util.DCWrapper;
//import io.chubao.joyqueue.toolkit.concurrent.EventBus;
//import io.chubao.joyqueue.toolkit.concurrent.EventListener;
//import io.chubao.joyqueue.toolkit.config.PropertySupplier;
//import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;
//import io.chubao.joyqueue.toolkit.lang.Close;
//import io.chubao.joyqueue.toolkit.lang.LifeCycle;
//import io.chubao.joyqueue.toolkit.service.Service;
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.lang3.RandomUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//import java.util.concurrent.Callable;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
///**
// * 1.启动nameServer
// * 2.器动管理端的http接口
// *
// * @author wylixiaobin
// * @date 2018/9/4
// */
//public class NameServerBak extends Service implements NameService, PropertySupplierAware, Type {
//    /**
//     * name server config
//     */
//    private NameServerConfig nameServerConfig;
//    /**
//     * transport factory
//     */
//    private NsrTransportServerFactory transportServerFactory;
//    /**
//     * transport server
//     */
//    private TransportServer transportServer;
//    /**
//     * manage server
//     */
//    private ManageServer manageServer;
//    /**
//     * 元数据管理器
//     */
//    private MetaManager metaManager;
//    /**
//     * properties
//     */
//    private PropertySupplier propertySupplier;
//    /**
//     * service provider
//     */
//    private ServiceProvider serviceProvider;
//    /**
//     * meta data cache
//     */
//    private MetaCache metaCache = new MetaCache();
//
//    /**
//     * 事件管理器
//     */
//    protected EventBus<NameServerEvent> eventManager = new EventBus<>("BROKER_NAMESERVER_EVENT_BUS");
//    /**
//     * service provider
//     */
//    public static ExtensionPoint<ServiceProvider, String> serviceProviderPoint = new ExtensionPointLazy<>(ServiceProvider.class);
//    private static final Logger logger = LoggerFactory.getLogger(NameServerBak.class);
//
//    private Cache<String, Map<TopicName, TopicConfig>> appTopicCache;
//
//    public NameServerBak() {
//        // do nothing
//    }
//
//    @Override
//    protected void validate() throws Exception {
//        super.validate();
//        if (nameServerConfig == null) {
//            nameServerConfig = new NameServerConfig(propertySupplier);
//        }
//        if (serviceProvider == null){
//            serviceProvider = loadServiceProvider(propertySupplier);
//        }
//        if (metaManager == null) {
//            metaManager = buildMetaManager();
//        }
//        if (transportServerFactory == null) {
//            this.transportServerFactory = new NsrTransportServerFactory(this);
//        }
//        if (manageServer == null) {
//            this.manageServer = buildManageServer();
//        }
//        if (transportServer == null){
//            this.transportServer = buildTransportServer();
//        }
//        if (appTopicCache == null) {
//            this.appTopicCache = CacheBuilder.newBuilder()
//                    .expireAfterWrite(nameServerConfig.getCacheExpireTime(), TimeUnit.MILLISECONDS)
//                    .build();
//        }
//        // TODO 临时
//        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
//            metaCache.appTokens.cleanUp();
//            metaCache.brokerConfigs.clear();
//            metaCache.consumerConfigs.clear();
//            metaCache.producerConfigs.clear();
//            metaCache.dataCenterMap.clear();
//            metaCache.topicConfigs.clear();
//        }, 0, 10, TimeUnit.MILLISECONDS);
//    }
//
//
//
//    @Override
//    public void doStart() throws Exception {
//        super.doStart();
//        this.manageServer.setManagerPort(nameServerConfig.getManagerPort());
//
//        this.metaManager.start();
//        this.eventManager.start();
//        this.transportServer.start();
//        this.manageServer.start();
//        logger.info("nameServer is started");
//    }
//
//    @Override
//    public void doStop() {
//        super.doStop();
//        try {
//            Close.close(manageServer);
//            Close.close(metaManager);
//            Close.close(eventManager);
//            Close.close(transportServer);
//            if(serviceProvider instanceof LifeCycle)
//            Close.close((LifeCycle )serviceProvider);
//        } finally {
//            logger.info("nameServer is stopped");
//        }
//    }
//
//    @Override
//    public List<TopicConfig> subscribe(List<Subscription> subscriptions, ClientType clientType) {
//        List<TopicConfig> configs = new ArrayList<>();
//        subscriptions.forEach(subscription -> {
//            TopicConfig topicConfig = subscribe(subscription, clientType);
//            if (null != topicConfig) {
//                configs.add(topicConfig);
//            }
//        });
//        return configs;
//    }
//
//    @Override
//    public TopicConfig subscribe(Subscription subscription, ClientType clientType) {
//        //TODO 考虑下这个
//        if (subscription.getType() == Subscription.Type.CONSUMPTION) {
//            TopicName topic = subscription.getTopic();
//            String app = subscription.getApp();
//            TopicConfig topicConfig = getTopicConfig(topic);
//            if (null == topicConfig){
//                return null;
//            }
//            Map<String, Consumer> consumerConfigMap = metaCache.consumerConfigs.get(topic);
//            if (null == consumerConfigMap){
//                consumerConfigMap = new ConcurrentHashMap<>();
//            }
//            if (!consumerConfigMap.containsKey(app)) {
//                Consumer consumer = metaManager.getConsumer(topic, app);
//                if (null == consumer) {
//                    consumer = new Consumer();
//                    consumer.setTopic(topic);
//                    consumer.setApp(app);
//
//                    consumer.setClientType(clientType);
//                    consumer = metaManager.addConsumer(consumer);
//                    consumerConfigMap.put(app, consumer);
//                }
//            }
//            return topicConfig;
//        } else if (subscription.getType() == Subscription.Type.PRODUCTION) {
//            TopicName topic = subscription.getTopic();
//            String app = subscription.getApp();
//            TopicConfig topicConfig = getTopicConfig(topic);
//            if (null == topicConfig) return null;
//            Map<String, Producer> producerConfigMap = metaCache.producerConfigs.get(topic);
//            if (null == producerConfigMap) producerConfigMap = new ConcurrentHashMap<>();
//            if (!producerConfigMap.containsKey(app)) {
//                Producer producer = metaManager.getProducer(topic, app);
//                if (null == producer) {
//                    producer = new Producer();
//                    producer.setTopic(topic);
//                    producer.setApp(app);
//
//                    producer.setClientType(clientType);
//                    producer = metaManager.addProducer(producer);
//                    producerConfigMap.put(app, producer);
//                }
//            }
//            return topicConfig;
//        } else {
//            throw new IllegalStateException("operation do not supported");
//        }
//    }
//
//    @Override
//    public void unSubscribe(Subscription subscription) {
//        if (subscription.getType() == Subscription.Type.CONSUMPTION) {
//            TopicConfig topicConfig = getTopicConfig(subscription.getTopic());
//            if (null == topicConfig){
//                return;
//            }
//            Map<String, Consumer> consumerConfigMap = metaCache.consumerConfigs.get(subscription.getTopic());
//            //todo 取消订阅
//            consumerConfigMap.remove(subscription.getApp());
//            metaManager.removeConsumer(subscription.getTopic(), subscription.getApp());
//        } else if (subscription.getType() == Subscription.Type.PRODUCTION) {
//            TopicConfig topicConfig = getTopicConfig(subscription.getTopic());
//            if (null == topicConfig) return;
//            Map<String, Producer> producerConfigMap = metaCache.producerConfigs.get(subscription.getTopic());
//            //todo 取消订阅
//            producerConfigMap.remove(subscription.getApp());
//            metaManager.removeProducer(subscription.getTopic(), subscription.getApp());
//        } else {
//            throw new IllegalStateException("operation do not supported");
//        }
//
//    }
//
//    @Override
//    public void unSubscribe(List<Subscription> subscriptions) {
//        subscriptions.forEach(subscription -> unSubscribe(subscription));
//    }
//
//
//    @Override
//    public void leaderReport(TopicName topic, int partitionGroup, int leaderBrokerId, Set<Integer> isrId, int termId) {
//        logger.info("Leader report, topic is {}, partition group is {}, leader is {}, term is {}",
//                topic, partitionGroup, leaderBrokerId, termId);
//        TopicConfig topicConfig = metaCache.topicConfigs.get(topic);
//        if (topicConfig == null) {
//            try {
//                topicConfig = reloadTopicConfig(topic);
//            } catch (Exception e) {
//                logger.warn("try to reload topic config failure, topic[{}]", topic.getFullName(), e);
//            }
//        }
//        if (topicConfig == null) {
//            return;
//        }
//        PartitionGroup group = null;
//        Iterator<PartitionGroup> groupIterator = topicConfig.getPartitionGroups().values().iterator();
//        while (groupIterator.hasNext()) {
//            PartitionGroup pgroup = groupIterator.next();
//            if (pgroup.getGroup() == partitionGroup) {
//                if (pgroup.getTerm() > termId || (pgroup.getTerm() == termId && leaderBrokerId == -1)) {
//                    logger.warn("Leader report for topic {} group {}, term {} less than current term {}, leaderId is {}",
//                            topic, partitionGroup, termId, pgroup.getTerm(), leaderBrokerId);
//                    return;
//                }
//                group = pgroup;
//                break;
//            }
//        }
//        if (null == group) {
//            throw new RuntimeException(String.format("topic[%s] group[%s] is not exist", topic, partitionGroup));
//        }
//        group.setIsrs(isrId);
//        group.setLeader(leaderBrokerId);
//        group.setTerm(termId);
//        metaManager.updatePartitionGroup(group);
//    }
//
//    @Override
//    public Broker getBroker(int brokerId) {
//        Broker broker = metaCache.brokerConfigs.get(brokerId);
//        if (null != broker) {
//            return broker;
//        }
//        return reloadBroker(brokerId, false);
//    }
//
//    @Override
//    public List<Broker> getAllBrokers() {
//        List<Broker> brokers = null;
//        try {
//            brokers = reloadBrokers();
//        } catch (Exception ignored) {
//        }
//
//        if (brokers == null) {
//            Collection col = metaCache.brokerConfigs.values();
//            brokers = (CollectionUtils.isEmpty(col) ? Collections.emptyList() : new ArrayList<>(col));
//        }
//
//        return brokers;
//    }
//
//    @Override
//    public void addTopic(Topic topic, List<PartitionGroup> partitionGroups) {
//        metaManager.addTopic(topic, partitionGroups);
//    }
//
//    @Override
//    public TopicConfig getTopicConfig(TopicName topic) {
//        // TODO 临时
//        TopicConfig topicConfig = metaCache.topicConfigs.get(topic);
//        if (null != topicConfig) {
//            return topicConfig;
//        } else {
//            return reloadTopicConfig(topic);
//        }
//    }
//
//    @Override
//    public Set<String> getAllTopicCodes() {
//        List<Topic> allTopics = metaManager.getAllTopicCodes();
//        if (null != allTopics) {
//            Set<String> topics = new HashSet<>();
//            allTopics.forEach(topic -> topics.add(topic.getName().getFullName()));
//            return topics;
//        } else {
//            return Collections.emptySet();
//        }
//    }
//
//
//    //TODO Ignite不可用
//    @Override
//    public Set<String> getTopics(String app, Subscription.Type subscribe) {
//        Set<String> topics = new HashSet<>();
//        if (null == subscribe) {
//            List<Producer> producers = metaManager.getProducer(app);
//            if (null != producers){
//                producers.forEach(producer -> topics.add(producer.getTopic().getFullName()));
//            }
//            List<Consumer> consumers = metaManager.getConsumer(app);
//            if (null != consumers){
//                consumers.forEach(consumer -> topics.add(consumer.getTopic().getFullName()));
//            }
//        } else {
//            switch (subscribe) {
//                case PRODUCTION:
//                    List<Producer> producers = metaManager.getProducer(app);
//                    if (null != producers){
//                        producers.forEach(producer -> topics.add(producer.getTopic().getFullName()));
//                    }
//                    break;
//                case CONSUMPTION:
//                    List<Consumer> consumers = metaManager.getConsumer(app);
//                    if (null != consumers){
//                        consumers.forEach(consumer -> topics.add(consumer.getTopic().getFullName()));
//                    }
//                    break;
//            }
//        }
//        return topics;
//    }
//
//    @Override
//    public Map<TopicName, TopicConfig> getTopicConfigByBroker(Integer brokerId) {
//        Map<TopicName,TopicConfig> map = new HashMap<>();
//        List<Replica> replicas = metaManager.getReplicaByBroker(brokerId);
//        for(Replica replica:replicas){
//            if(!map.containsKey(replica.getTopic())){
//                map.put(replica.getTopic(),getTopicConfig(replica.getTopic()));
//            }
//        }
//        return map;
//    }
//
//    @Override
//    public List<Replica> getReplicaByBroker(Integer brokerId) {
//        return metaManager.getReplicaByBroker(brokerId);
//    }
//
//    @Override
//    public AppToken getAppToken(String app, String token) {
//        AppToken appToken = null;
//        try {
//            appToken = metaCache.appTokens.get(createAppTokenCacheKey(app, token), new Callable<AppToken>() {
//                @Override
//                public AppToken call() throws Exception {
//                    return metaManager.findAppToken(app, token);
//                }
//            });
//        } catch (ExecutionException e) {
//            logger.error("find app token exception, app: {}, token: {}", app, token, e);
//            return null;
//        }
//        return appToken;
//    }
//
//
//
//
//    // TODO brokerId改成long
//    @Override
//    public Broker register(Integer brokerId, String brokerIp, Integer port) {
//        Broker broker = null;
//        if (null == brokerId) {
//            broker = metaManager.getBrokerByIpAndPort(brokerIp, port);
//            if (null == broker) {
//                brokerId = generateBrokerId();
//                broker = new Broker();
//                broker.setId(brokerId);
//                broker.setIp(brokerIp);
//                DataCenter dataCenter = getDataCenter(brokerIp);
//                broker.setDataCenter(dataCenter.getCode());
//                broker.setPort(port);
//                broker.setRetryType(Broker.DEFAULT_RETRY_TYPE);
//                broker.setPermission(Broker.PermissionEnum.FULL);
//                metaManager.addBroker(broker);
//                logger.info("register broker success broker.id {}", brokerId);
//            } else {
//                brokerId = broker.getId();
//            }
//        }
//        if (null != broker) {
//            metaCache.brokerConfigs.put(brokerId, broker);
//        }
//        //TODO 并未去更新broker的IP
//        broker = reloadBroker(brokerId, true);
//        if (null != broker) {
//            broker.setIp(brokerIp);
//            broker.setPort(port);
//            broker.setDataCenter(getDataCenter(brokerIp).getCode());
//            metaManager.updateBroker(broker);
//        }
//
//        return broker;
//    }
//
//    protected int generateBrokerId() {
//        int id = 0;
//        while (id <= 0) {
//            int result = (int) (System.nanoTime() % 1000000000);
//            id = result + RandomUtils.nextInt(0, result);
//        }
//        return id;
//    }
//
//    @Override
//    public Producer getProducerByTopicAndApp(TopicName topic, String app) {
//        Map<String, Producer> cachedAppProducers = metaCache.producerConfigs.get(topic);
//        if (cachedAppProducers != null && cachedAppProducers.containsKey(app)) {
//            return cachedAppProducers.get(app);
//        } else {
//            Producer producer = metaManager.getProducer(topic, app);
//            if (null != producer) {
//                if (cachedAppProducers == null) {
//                    cachedAppProducers = new ConcurrentHashMap<>();
//                    Map<String, Producer> preCache = metaCache.producerConfigs.putIfAbsent(topic, cachedAppProducers);
//                    if (preCache != null) {
//                        cachedAppProducers = preCache;
//                    }
//                }
//                cachedAppProducers.put(app, producer);
//            }
//            return producer;
//        }
//    }
//
//    @Override
//    public Consumer getConsumerByTopicAndApp(TopicName topic, String app) {
//        Map<String, Consumer> cachedAppConsumers = metaCache.consumerConfigs.get(topic);
//        if (cachedAppConsumers != null && cachedAppConsumers.containsKey(app)) {
//            return cachedAppConsumers.get(app);
//        } else {
//            Consumer consumer = metaManager.getConsumer(topic, app);
//            if (null != consumer) {
//                if (cachedAppConsumers == null) {
//                    cachedAppConsumers = new ConcurrentHashMap();
//                }
//
//                Map<String, Consumer> preCached = metaCache.consumerConfigs.putIfAbsent(topic, cachedAppConsumers);
//                if (preCached != null) {
//                    cachedAppConsumers = preCached;
//                }
//                cachedAppConsumers.put(app, consumer);
//            }
//            return consumer;
//        }
//    }
//
//    @Override
//    public Map<TopicName, TopicConfig> getTopicConfigByApp(String subscribeApp, Subscription.Type subscribe) {
//        if (nameServerConfig.getCacheEnable()) {
//            try {
//                return appTopicCache.get(subscribeApp + "_" + String.valueOf(subscribe), new Callable<Map<TopicName, TopicConfig>>() {
//                    @Override
//                    public Map<TopicName, TopicConfig> call() throws Exception {
//                        return doGetTopicConfigByApp(subscribeApp, subscribe);
//                    }
//                });
//            } catch (ExecutionException e) {
//                logger.error("getTopicConfigByApp exception, subscribeApp: {}, subscribe: {}",
//                        subscribeApp, subscribe);
//                return Maps.newHashMap();
//            }
//        } else {
//            return doGetTopicConfigByApp(subscribeApp, subscribe);
//        }
//    }
//
//    protected Map<TopicName, TopicConfig> doGetTopicConfigByApp(String subscribeApp, Subscription.Type subscribe) {
//        Map<TopicName, TopicConfig> appTopicConfigs = new HashMap<>();
//
//        List<? extends Subscription> subscriptions = null;
//        switch (subscribe) {
//            case CONSUMPTION:
//                subscriptions = metaManager.getConsumer(subscribeApp);
//                break;
//            case PRODUCTION:
//                subscriptions = metaManager.getProducer(subscribeApp);
//                break;
//        }
//
//        if (null != subscriptions) {
//            subscriptions.forEach(p -> {
//                TopicConfig topicConfig = getTopicConfig(p.getTopic());
//                if (null != topicConfig) {
//                    appTopicConfigs.put(p.getTopic(), topicConfig);
//                }
//            });
//        }
//        return appTopicConfigs;
//    }
//
//    @Override
//    public boolean hasSubscribe(String subscribeApp, Subscription.Type subscribe) {
//        //TODO Ignite 不可用
//        switch (subscribe) {
//            case CONSUMPTION:
//                List<Consumer> consumers = metaManager.getConsumer(subscribeApp);
//                return (null != consumers && consumers.size() > 0) ? true : false;
//            case PRODUCTION:
//                List<Producer> producers = metaManager.getProducer(subscribeApp);
//                return (null != producers && producers.size() > 0) ? true : false;
//        }
//        return false;
//    }
//
//    @Override
//    public DataCenter getDataCenter(String ip) {
//        if (metaCache.dataCenterMap.isEmpty()) {
//            Collection<DataCenter> dcs = metaManager.getAllDataCenter();
//            //应该不需要同步
//            // synchronized (metaCache.dataCenterMap) {
//            if (null != dcs) {
//                dcs.forEach(dataCenter -> {
//                    metaCache.dataCenterMap.put(dataCenter.getCode(), new DCWrapper(dataCenter));
//                });
//            }
//            //}
//        }
//        Optional<DCWrapper> optional = metaCache.dataCenterMap.values().stream().filter(dataCenter -> dataCenter.match(ip)).findFirst();
//        if (optional.isPresent()) {
//            return optional.get().getDataCenter();
//        }
//        return DataCenter.DEFAULT;
//    }
//
//    @Override
//    public String getConfig(String group, String key) {
//        Config config = metaManager.getConfig(group, key);
//        return config == null ? null : config.getValue();
//    }
//
//    @Override
//    public List<Config> getAllConfigs() {
//        return metaManager.getAllConfigs();
//    }
//
//    @Override
//    public List<Broker> getBrokerByRetryType(String retryType) {
//        return metaManager.getBrokerByRetryType(retryType);
//    }
//
//    @Override
//    public List<Consumer> getConsumerByTopic(TopicName topic) {
//        return metaManager.getConsumerByTopic(topic);
//    }
//
//    @Override
//    public List<Producer> getProducerByTopic(TopicName topic) {
//        return metaManager.getProducerByTopic(topic);
//    }
//
//    @Override
//    public void addListener(EventListener<NameServerEvent> listener) {
//        //TODO 是否需要全量更新一下
//        eventManager.addListener(listener);
//    }
//
//    @Override
//    public void removeListener(EventListener<NameServerEvent> listener) {
//        eventManager.removeListener(listener);
//    }
//
//    @Override
//    public void addEvent(NameServerEvent event) {
//        eventManager.add(event);
//    }
//
//    private TopicConfig reloadTopicConfig(TopicName topicCode) {
//        Topic topic = metaManager.getTopicByName(topicCode);
//        if (null == topic) {
//            return null;
//        }
//        List<PartitionGroup> partitionGroups = metaManager.getPartitionGroupByTopic(topicCode);
//        TopicConfig old = metaCache.topicConfigs.get(topicCode);
//        TopicConfig topicConfig = TopicConfig.toTopicConfig(topic);
//        topicConfig.setPartitionGroups(partitionGroups.stream().collect(Collectors.toMap(PartitionGroup::getGroup, group -> group)));
//        metaCache.topicConfigs.put(topicCode, topicConfig);
//        if (null != partitionGroups) {
//            partitionGroups.forEach(group -> {
//                Map<Integer, Broker> brokerMap = new HashMap<>();
//                group.getReplicas().forEach(brokerId -> {
//                    if (!brokerMap.containsKey(brokerId)) {
//                        brokerMap.put(brokerId, reloadBroker(brokerId, false));
//                    }
//                });
//                group.setBrokers(brokerMap);
//            });
//        }
//
//        if (null != old) {
//            Set<Integer> removeBrokerIds = old.fetchAllBrokerIds();
//            removeBrokerIds.removeAll(topicConfig.fetchAllBrokerIds());
//        }
//        /**
//         * 初始化 consumerConfigs
//         */
//        if (!metaCache.consumerConfigs.containsKey(topicCode)) {
//            metaCache.consumerConfigs.put(topicCode, new HashMap<>());
//        }
//        if (!metaCache.producerConfigs.containsKey(topicCode)) {
//            metaCache.producerConfigs.put(topicCode, new HashMap<>());
//        }
//        return topicConfig;
//    }
//
//    private Consumer reloadConsumer(TopicName topic, String app) {
//        Consumer consumer = metaManager.getConsumer(topic, app);
//        if (null != consumer) {
//            Map<String, Consumer> cachedConsumers = metaCache.consumerConfigs.get(topic);
//            if (cachedConsumers == null) {
//                cachedConsumers = new HashMap<>();
//                Map<String, Consumer> preCache = metaCache.consumerConfigs.putIfAbsent(topic, cachedConsumers);
//                if (preCache != null) {
//                    cachedConsumers = preCache;
//                }
//            }
//            cachedConsumers.put(app, consumer);
//        }
//        return consumer;
//    }
//
//    private Producer reloadProducer(TopicName topic, String app) {
//        Producer producer = metaManager.getProducer(topic, app);
//        if (null != producer) {
//            Map<String, Producer> cachedProducers = metaCache.producerConfigs.get(topic);
//            if (cachedProducers == null) {
//                cachedProducers = new HashMap<>();
//                Map preCache = metaCache.producerConfigs.putIfAbsent(topic, cachedProducers);
//                if (preCache != null) {
//                    cachedProducers = preCache;
//                }
//            }
//            cachedProducers.put(app, producer);
//        }
//        return producer;
//    }
//
//    private Broker reloadBroker(Integer brokerId, boolean reLoadTopic) {
//        Broker broker = metaManager.getBrokerById(brokerId);
//        if (null == broker) {
//            return null;
//        }
//        metaCache.brokerConfigs.put(broker.getId(), broker);
//        if (reLoadTopic) {
//            Set<TopicName> topics = metaManager.getTopicByBroker(brokerId);
//            if (null != topics){
//                topics.forEach(topic -> {
//                    reloadTopicConfig(topic);
//                });
//            }
//        }
//        return broker;
//    }
//
//    private List<Broker> reloadBrokers() {
//        List<Broker> brokers = metaManager.getAllBrokers();
//        for (Broker broker : brokers) {
//            metaCache.brokerConfigs.put(broker.getId(), broker);
//        }
//        return brokers;
//    }
//
//    @Override
//    public String type() {
//        return "server";
//    }
//
//    @Override
//    public void setSupplier(PropertySupplier supplier) {
//        this.propertySupplier = supplier;
//    }
//
//    // TODO 参数化
//    protected class MetaCache {
//        /**
//         * app tokens
//         */
//        private Cache<String, AppToken> appTokens = CacheBuilder.newBuilder()
//                .expireAfterWrite(1000 * 60 * 1, TimeUnit.MILLISECONDS)
//                .maximumSize(10240)
//                .build();
//        /**
//         * 数据中心
//         */
//        private Map<String, DCWrapper> dataCenterMap = new ConcurrentHashMap<>();
//        /**
//         * broker配置
//         * Map<datacenter@ip@port,BrokerConfig>
//         */
//        private Map<Integer, Broker> brokerConfigs = new ConcurrentHashMap<>();
//        /**
//         * 主题配置
//         */
//        private Map<TopicName, TopicConfig> topicConfigs = new ConcurrentHashMap<>();
//        /**
//         * 消费配置
//         */
//        private Map<TopicName, Map<String, Consumer>> consumerConfigs = new ConcurrentHashMap<>();
//        /**
//         * 发送配置
//         */
//        private Map<TopicName, Map<String, Producer>> producerConfigs = new ConcurrentHashMap<>();
//
//    }
//
//    private ManageServer buildManageServer() {
//        TopicService topicService = serviceProvider.getService(TopicService.class);
//        BrokerService brokerService = serviceProvider.getService(BrokerService.class);
//        ConsumerService consumerService = serviceProvider.getService(ConsumerService.class);
//        ProducerService producerService = serviceProvider.getService(ProducerService.class);
//        ConfigService configService = serviceProvider.getService(ConfigService.class);
//        AppTokenService appTokenService = serviceProvider.getService(AppTokenService.class);
//        DataCenterService dataCenterService = serviceProvider.getService(DataCenterService.class);
//        NamespaceService namespaceService = serviceProvider.getService(NamespaceService.class);
//        PartitionGroupService partitionGroupService = serviceProvider.getService(PartitionGroupService.class);
//        PartitionGroupReplicaService partitionGroupReplicaService = serviceProvider.getService(PartitionGroupReplicaService.class);
//
//        Preconditions.checkArgument(brokerService != null, "broker service can not be null");
//        Preconditions.checkArgument(topicService != null, "topic service can not be null");
//        Preconditions.checkArgument(consumerService != null, "consumer service can not be null");
//        Preconditions.checkArgument(producerService != null, "producer service can not be null");
//        Preconditions.checkArgument(appTokenService != null, "appToken service can not be null");
//        Preconditions.checkArgument(namespaceService != null, "namespace service can not be null");
//        Preconditions.checkArgument(dataCenterService != null, "datacenter service can not be null");
//        Preconditions.checkArgument(partitionGroupReplicaService != null, "replica service can not be null");
//        Preconditions.checkArgument(partitionGroupService != null, "partitionGroup service can not be null");
//        return new ManageServer(topicService, producerService, consumerService,
//                brokerService, configService, appTokenService, dataCenterService,
//                namespaceService, partitionGroupService, partitionGroupReplicaService);
//
//    }
//
//
//    private MetaManager buildMetaManager() {
//        TopicService topicService = serviceProvider.getService(TopicService.class);
//        ConfigService configService = serviceProvider.getService(ConfigService.class);
//        BrokerService brokerService = serviceProvider.getService(BrokerService.class);
//        ConsumerService consumerService = serviceProvider.getService(ConsumerService.class);
//        ProducerService producerService = serviceProvider.getService(ProducerService.class);
//        AppTokenService appTokenService = serviceProvider.getService(AppTokenService.class);
//        DataCenterService dataCenterService = serviceProvider.getService(DataCenterService.class);
//        PartitionGroupService partitionGroupService = serviceProvider.getService(PartitionGroupService.class);
//        PartitionGroupReplicaService partitionGroupReplicaService = serviceProvider.getService(PartitionGroupReplicaService.class);
//
//        return new MetaManager(configService, topicService, brokerService,
//                consumerService, producerService, partitionGroupService,
//                partitionGroupReplicaService, appTokenService, dataCenterService);
//
//    }
//
//
//
//    private TransportServer buildTransportServer(){
//        ServerConfig serverConfig = nameServerConfig.getServerConfig();
//        serverConfig.setPort(nameServerConfig.getServicePort());
//        serverConfig.setAcceptThreadName("joyqueue-nameserver-accept-eventLoop");
//        serverConfig.setIoThreadName("joyqueue-nameserver-io-eventLoop");
//        return transportServerFactory.bind(serverConfig, serverConfig.getHost(), serverConfig.getPort());
//    }
//
//
//    private ServiceProvider loadServiceProvider(PropertySupplier propertySupplier) throws Exception{
//        ServiceProvider serviceProvider = serviceProviderPoint.get();
//        Preconditions.checkArgument(serviceProvider != null, "service provider can not be null.");
//        if (serviceProvider instanceof PropertySupplierAware) {
//            ((PropertySupplierAware) serviceProvider).setSupplier(propertySupplier);
//        }
//
//        if (serviceProvider instanceof LifeCycle){
//            ((LifeCycle) serviceProvider).start();
//        }
//        return serviceProvider;
//    }
//
//    private String createAppTokenCacheKey(String app, String token) {
//        return app + "@" + token;
//    }
//
//}
