package com.jd.journalq.nsr;

import com.jd.journalq.common.domain.*;
import com.jd.journalq.common.event.*;
import com.jd.journalq.common.network.event.TransportEvent;
import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.TransportServer;
import com.jd.journalq.common.network.transport.config.ServerConfig;
import com.jd.journalq.nsr.config.NameServerConfig;
import com.jd.journalq.nsr.message.MessageListener;
import com.jd.journalq.nsr.message.Messenger;
import com.jd.journalq.nsr.network.NsrTransportServerFactory;
import com.jd.journalq.nsr.service.*;
import com.jd.journalq.nsr.util.DCWrapper;
import com.jd.journalq.nsr.service.*;
import com.jd.journalq.toolkit.concurrent.EventBus;
import com.jd.journalq.toolkit.concurrent.EventListener;
import com.jd.journalq.toolkit.config.PropertySupplier;
import com.jd.journalq.toolkit.config.PropertySupplierAware;
import com.jd.journalq.toolkit.lang.Close;
import com.jd.journalq.toolkit.lang.LifeCycle;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.service.Service;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import com.jd.laf.extension.SpiLoader;
import com.jd.laf.extension.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 1.启动nameServer
 * 2.器动管理端的http接口
 *
 * @author wylixiaobin
 * Date: 2018/9/4
 */
public class NameServer extends Service implements NameService, PropertySupplierAware, Type {
    /**
     * service provider
     */
    ExtensionPoint<ServiceProvider, String> serviceProviderPoint = new ExtensionPointLazy<>(ServiceProvider.class, SpiLoader.INSTANCE, null, null);
    private static final Logger logger = LoggerFactory.getLogger(NameServer.class);
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
     * 事件管理器
     */
    protected EventBus<NameServerEvent> eventManager = new EventBus<>("BROKER_NAMESERVER_EVENT_BUS");

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

    public NameServer() {
    }

    @Override
    protected void validate() throws Exception {
        super.validate();
        if (nameServerConfig == null) {
            nameServerConfig = new NameServerConfig(propertySupplier);
        }
        if (transportServerFactory == null) {
            this.transportServerFactory = new NsrTransportServerFactory(this);
        }
        if (serviceProvider == null){
           serviceProvider = loadServiceProvider(propertySupplier);
        }

        if (manageServer == null) {
            this.manageServer = buildManageServer();
        }

        if (metaManager == null) {
            metaManager = buildMetaManager();
        }
        if (listener == null) {
            listener = new MetaDataListener();
        }
        
        
        
        metaManager.addListener(listener);
        transportServerFactory.addListener(new BrokerTranSportManager());
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


    @Override
    public void doStart() throws Exception {
        super.doStart();
        this.manageServer.setManager_port(nameServerConfig.getManagerPort());
        manageServer.start();
        ServerConfig serverConfig = nameServerConfig.getServerConfig();
        this.transportServer = transportServerFactory.bind(serverConfig, serverConfig.getHost(), serverConfig.getPort());
        this.transportServer.start();
        logger.info("nameServer is started");

        ///
        metaManager.start();
        eventManager.start();
        logger.info("nameService is started");
    }

    @Override
    public void doStop() {
        super.doStop();
        try {
            Close.close(manageServer);
            Close.close(metaManager);
            Close.close(eventManager);
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
        if (subscription.getType() == Subscription.Type.CONSUMPTION) {
            TopicName topic = subscription.getTopic();
            String app = subscription.getApp();
            TopicConfig topicConfig = getTopicConfig(topic);
            if (null == topicConfig) return null;
            Map<String, Consumer> consumerConfigMap = metaCache.consumerConfigs.get(topic);
            if (null == consumerConfigMap) consumerConfigMap = new ConcurrentHashMap<>();
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
        } else {
            throw new IllegalStateException("operation do not supported");
        }
    }

    @Override
    public void unSubscribe(Subscription subscription) {
        if (subscription.getType() == Subscription.Type.CONSUMPTION) {
            TopicConfig topicConfig = getTopicConfig(subscription.getTopic());
            if (null == topicConfig) return;
            Map<String, Consumer> consumerConfigMap = metaCache.consumerConfigs.get(subscription.getTopic());
            //todo 取消订阅
            consumerConfigMap.remove(subscription.getApp());
            metaManager.removeConsumer(subscription.getTopic(), subscription.getApp());
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

        TopicConfig topicConfig = getTopicConfig(topic);
        if (null == topicConfig) {
            return;
        }
        PartitionGroup group = null;
        Iterator<PartitionGroup> groupIterator = topicConfig.getPartitionGroups().values().iterator();
        while (groupIterator.hasNext()) {
            PartitionGroup pgroup = groupIterator.next();
            if (pgroup.getGroup() == partitionGroup) {
                if (pgroup.getTerm() > termId || (pgroup.getTerm() == termId && leaderBrokerId == -1)) {
                    logger.info("Leader report for topic {} group {}, term {} less than current term {}, leaderId is {}",
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
        if (null != broker) return broker;
        return reloadBroker(brokerId, false);
    }

    @Override
    public List<Broker> getAllBrokers() {
        return metaManager.getAllBrokers();
    }

    public void addConsumer(Consumer consumer) {
        metaManager.addConsumer(consumer);
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


    @Override
    public Set<String> getTopics(String app, Subscription.Type subscribe) {
        Set<String> topics = new HashSet<>();
        if (null == subscribe) {
            List<Producer> producers = metaManager.getProducer(app);
            if (null != producers) producers.forEach(producer -> topics.add(producer.getTopic().getFullName()));
            List<Consumer> consumers = metaManager.getConsumer(app);
            if (null != consumers) consumers.forEach(consumer -> topics.add(consumer.getTopic().getFullName()));
        } else {
            switch (subscribe) {
                case PRODUCTION:
                    List<Producer> producers = metaManager.getProducer(app);
                    if (null != producers) producers.forEach(producer -> topics.add(producer.getTopic().getFullName()));
                    break;
                case CONSUMPTION:
                    List<Consumer> consumers = metaManager.getConsumer(app);
                    if (null != consumers) consumers.forEach(consumer -> topics.add(consumer.getTopic().getFullName()));
                    break;
            }
        }
        return topics;
    }

    @Override
    public Map<TopicName, TopicConfig> getTopicConfigByBroker(Integer brokerId) {
        return metaCache.brokerTopicConfigs.get(brokerId);
    }

    @Override
    public List<Replica> getReplicaByBroker(Integer brokerId) {
        return metaManager.getReplicaByBroker(brokerId);
    }

    @Override
    public AppToken getAppToken(String app, String token) {
        return metaManager.findByAppAndToken(app, token);
    }

    @Override
    public Broker register(Integer brokerId, String brokerIp, Integer port) {
        Broker broker = null;
        if (null == brokerId) {
            broker = metaManager.getBrokerByIpAndPort(brokerIp, port);
            if (null == broker) {
                //TODO broker ID 生成逻辑不严谨，重复几率大
                brokerId = Integer.parseInt(String.valueOf(System.currentTimeMillis() / 1000));
                broker = new Broker();
                broker.setId(brokerId);
                broker.setIp(brokerIp);
                DataCenter dataCenter = getDataCenter(brokerIp);
                broker.setDataCenter(dataCenter.getCode());
                broker.setPort(port);
                broker.setRetryType(Broker.DEFAULT_RETRY_TYPE);
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
            metaCache.registerBroker.put(brokerId, broker);
        }
        return broker;
    }

    @Override
    public Producer getProducerByTopicAndApp(TopicName topic, String app) {
        if (metaCache.producerConfigs.containsKey(topic) && metaCache.producerConfigs.get(topic).containsKey(app)) {
            return metaCache.producerConfigs.get(topic).get(app);
        } else {
            Producer producer = metaManager.getProducer(topic, app);
            if (null != producer) {
                if (!metaCache.producerConfigs.containsKey(topic)) {
                    metaCache.producerConfigs.put(topic, new HashMap<>()).put(app, producer);
                } else {
                    metaCache.producerConfigs.get(topic).putIfAbsent(app, producer);
                }
            }
            return producer;
        }
    }

    @Override
    public Consumer getConsumerByTopicAndApp(TopicName topic, String app) {

        if (metaCache.consumerConfigs.containsKey(topic) && metaCache.consumerConfigs.get(topic).containsKey(app)) {
            return metaCache.consumerConfigs.get(topic).get(app);
        } else {
            Consumer consumer = metaManager.getConsumer(topic, app);
            if (null != consumer) {
                if (!metaCache.consumerConfigs.containsKey(topic)) {
                    metaCache.consumerConfigs.put(topic, new HashMap<>()).put(app, consumer);
                } else {
                    metaCache.consumerConfigs.get(topic).putIfAbsent(app, consumer);
                }
            }
            return consumer;
        }
    }

    @Override
    public Map<TopicName, TopicConfig> getTopicConfigByApp(String subscribeApp, Subscription.Type subscribe) {
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
            synchronized (metaCache.dataCenterMap) {
                if (null != dcs) dcs.forEach(dataCenter -> {
                    metaCache.dataCenterMap.put(dataCenter.getCode(), new DCWrapper(dataCenter));
                });
            }
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
        if (null == config) return null;
        return config.getValue();
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
                    if (!brokerMap.containsKey(brokerId)) brokerMap.put(brokerId, reloadBroker(brokerId, false));
                    if (metaCache.brokerTopicConfigs.containsKey(brokerId)) {
                        metaCache.brokerTopicConfigs.get(brokerId).put(topicCode, topicConfig);
                    }
                });
                group.setBrokers(brokerMap);
            });
        }
        ;
        if (null != old) {
            Set<Integer> removeBrokerids = old.fetchAllBrokerIds();
            removeBrokerids.removeAll(topicConfig.fetchAllBrokerIds());
            removeBrokerids.forEach(brokerId -> {
                if (metaCache.brokerTopicConfigs.containsKey(brokerId)) {
                    metaCache.brokerTopicConfigs.get(brokerId).remove(topicCode);
                }
            });

        }
        /**
         * 初始化 consumerConfigs
         */
        if (!metaCache.consumerConfigs.containsKey(topicCode))
            metaCache.consumerConfigs.put(topicCode, new HashMap<>());
        if (!metaCache.producerConfigs.containsKey(topicCode))
            metaCache.producerConfigs.put(topicCode, new HashMap<>());
        return topicConfig;
    }

    private Consumer reloadConsumer(TopicName topic, String app) {
        Consumer consumer = metaManager.getConsumer(topic, app);
        if (null != consumer) {
            if (!metaCache.consumerConfigs.containsKey(topic)) {
                Map<String, Consumer> map = new HashMap<>();
                map.put(app, consumer);
                metaCache.consumerConfigs.put(topic, map);
            } else {
                metaCache.consumerConfigs.get(topic).put(app, consumer);
            }
        }
        return consumer;
    }

    private Producer reloadProducer(TopicName topic, String app) {
        Producer producer = metaManager.getProducer(topic, app);
        if (null != producer) {
            if (!metaCache.producerConfigs.containsKey(topic)) {
                Map<String, Producer> map = new HashMap<>();
                map.put(app, producer);
                metaCache.producerConfigs.put(topic, map);
            } else {
                metaCache.producerConfigs.get(topic).put(app, producer);
            }
        }
        return producer;
    }

    private Broker reloadBroker(Integer brokerId, boolean loadTopic) {
        Broker broker = metaManager.getBrokerById(brokerId);
        if (null == broker) {
            return null;
        }
        metaCache.brokerConfigs.put(broker.getId(), broker);
        if (loadTopic) {
            Set<TopicName> topics = metaManager.getTopicByBroker(brokerId);
            Map<TopicName, TopicConfig> bts = new HashMap<>();
            if (null != topics) topics.forEach(topic -> {
                TopicConfig topicConfig = getTopicConfig(topic);
                metaCache.topicConfigs.put(topic, topicConfig);
                bts.put(topicConfig.getName(), topicConfig);
            });

            //TODO 有没有并发问题
            metaCache.brokerTopicConfigs.put(broker.getId(), bts);
        }
        return broker;
    }

    @Override
    public String type() {
        return "server";
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.propertySupplier = supplier;
    }

    public class BrokerTranSportManager implements EventListener<TransportEvent> {
        @Override
        public void onEvent(TransportEvent event) {
            Transport transport = event.getTransport();
            String broker = transport.attr().get("broker.id");
            if(null==broker)return;
            Integer brokerId = Integer.valueOf(broker);
            switch (event.getType()) {
                case CONNECT:
                    metaCache.registerBroker.put(brokerId,getBroker(brokerId));
                case EXCEPTION:
                case CLOSE:
                    metaCache.registerBroker.remove(brokerId);
                    break;
                default:
                    break;
            }
        }
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
                    logger.info("UPDATE_TOPIC [{}],brokers[{}]", topicConfig1, metaCache.brokerConfigs);
                    if (null != topicConfig1) {
                        topicConfig1.fetchAllBroker().keySet().forEach(brokerId -> {
                            if (metaCache.registerBroker.containsKey(brokerId)) {
                                eventManager.add(new NameServerEvent(event, brokerId));
                            }
                        });
                    }
                    break;
                case REMOVE_TOPIC:
                    TopicName topic2 = ((TopicEvent) event).getTopic();
                    TopicConfig topicConfig2 = metaCache.topicConfigs.remove(topic2);
                    logger.info("REMOVE_TOPIC [{}],brokers[{}]", topic2, metaCache.brokerConfigs);
                    if (null != topicConfig2) {
                        topicConfig2.fetchAllBroker().keySet().forEach(brokerId -> {
                            if (metaCache.registerBroker.containsKey(brokerId)) {
                                eventManager.add(new NameServerEvent(event, brokerId));
                            }
                        });
                    }
                    break;
                case ADD_PARTITION_GROUP:
                case UPDATE_PARTITION_GROUP:
                    TopicName topic3 = ((PartitionGroupEvent) event).getTopic();
                    TopicConfig topicConfig3Old = getTopicConfig(topic3);
                    TopicConfig topicConfig3 = reloadTopicConfig(topic3);
                    logger.info("UPDATE_PARTITION_GROUP [{}],brokers[{}]", topicConfig3, metaCache.brokerConfigs);
                    Set<Integer> broker3 = new HashSet<>();
                    if (null != topicConfig3) broker3.addAll(topicConfig3.fetchAllBrokerIds());
                    if (null != topicConfig3Old) broker3.addAll(topicConfig3Old.fetchAllBrokerIds());
                    broker3.forEach(brokerId -> {
                        if (metaCache.registerBroker.containsKey(brokerId)) {
                            eventManager.add(new NameServerEvent(event, brokerId));
                        }
                    });
                    break;
                case REMOVE_PARTITION_GROUP:
                    TopicName topic4 = ((PartitionGroupEvent) event).getTopic();
                    TopicConfig topicConfig4Old = getTopicConfig(topic4);
                    TopicConfig topicConfig4 = reloadTopicConfig(topic4);
                    logger.info("REMOVE_PARTITION_GROUP [{}],brokers[{}]", topicConfig4, metaCache.brokerConfigs);
                    if (null != topicConfig4) {
                        (null == topicConfig4Old ? topicConfig4 : topicConfig4Old).fetchAllBrokerIds().forEach(brokerId -> {
                            if (metaCache.registerBroker.containsKey(brokerId)) {
                                eventManager.add(new NameServerEvent(event, brokerId));
                            }
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
                            if (metaCache.registerBroker.containsKey(brokerId)) {
                                eventManager.add(new NameServerEvent(event, brokerId));
                            }
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
                            if (metaCache.registerBroker.containsKey(brokerId)) {
                                eventManager.add(new NameServerEvent(event, brokerId));
                            }
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
                            if (metaCache.registerBroker.containsKey(brokerId)) {
                                eventManager.add(new NameServerEvent(event, brokerId));
                            }
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
                            if (metaCache.registerBroker.containsKey(brokerId)) {
                                eventManager.add(new NameServerEvent(event, brokerId));
                            }
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
                    eventManager.add(new NameServerEvent(event, null));
                    break;
                case REMOVE_CONFIG:
                    logger.info("REMOVE_DATACENTER [{}]", event);
                    eventManager.add(new NameServerEvent(event, null));
                    break;
                default:
                    break;
            }
        }
    }

    protected class MetaCache {
        /**
         * 主题配置
         */
        private Map<TopicName, TopicConfig> topicConfigs = new ConcurrentHashMap<>();
        /**
         * broker配置
         * Map<datacenter@ip@port,BrokerConfig>
         */
        private Map<Integer, Broker> brokerConfigs = new ConcurrentHashMap<>();

        /**
         * broker主题配置
         */
        private Map<Integer, Map<TopicName, TopicConfig>> brokerTopicConfigs = new ConcurrentHashMap<>();
        /**
         * 消费配置
         */
        private Map<TopicName, Map<String, Consumer>> consumerConfigs = new ConcurrentHashMap<>();
        /**
         * 发送配置
         */
        private Map<TopicName, Map<String, Producer>> producerConfigs = new ConcurrentHashMap<>();
        /**
         * 数据中心
         */
        private Map<String, DCWrapper> dataCenterMap = new ConcurrentHashMap<>();
        /**
         *
         */
        private Map<Integer, Broker> registerBroker = new ConcurrentHashMap<>();

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
        PartitionGroupReplicaService partitionGroupReplicaService = serviceProvider.getService(PartitionGroupReplicaService.class);
        PartitionGroupService partitionGroupService = serviceProvider.getService(PartitionGroupService.class);

        Preconditions.checkArgument(brokerService != null, "broker service can not be null");
        Preconditions.checkArgument(topicService != null, "topic service can not be null");
        Preconditions.checkArgument(consumerService != null, "consumer service can not be null");
        Preconditions.checkArgument(producerService != null, "producer service can not be null");
        Preconditions.checkArgument(appTokenService != null, "appToken service can not be null");
        Preconditions.checkArgument(dataCenterService != null, "datacenter service can not be null");
        Preconditions.checkArgument(namespaceService != null, "namespace service can not be null");
        Preconditions.checkArgument(partitionGroupReplicaService != null, "replica service can not be null");
        Preconditions.checkArgument(partitionGroupService != null, "partitionGroup service can not be null");
        return new ManageServer(topicService, producerService, consumerService, brokerService, configService, appTokenService, dataCenterService, namespaceService, partitionGroupService, partitionGroupReplicaService);

    }


    private MetaManager buildMetaManager() {
        TopicService topicService = serviceProvider.getService(TopicService.class);
        BrokerService brokerService = serviceProvider.getService(BrokerService.class);
        ConsumerService consumerService = serviceProvider.getService(ConsumerService.class);
        ProducerService producerService = serviceProvider.getService(ProducerService.class);
        PartitionGroupService partitionGroupService = serviceProvider.getService(PartitionGroupService.class);
        PartitionGroupReplicaService partitionGroupReplicaService = serviceProvider.getService(PartitionGroupReplicaService.class);
        ConfigService configService = serviceProvider.getService(ConfigService.class);
        AppTokenService appTokenService = serviceProvider.getService(AppTokenService.class);
        Messenger messenger = serviceProvider.getService(Messenger.class);
        DataCenterService dataCenterService = serviceProvider.getService(DataCenterService.class);

        return new MetaManager(messenger, configService, topicService, brokerService, consumerService, producerService, partitionGroupService, partitionGroupReplicaService, appTokenService, dataCenterService);

    }
}
