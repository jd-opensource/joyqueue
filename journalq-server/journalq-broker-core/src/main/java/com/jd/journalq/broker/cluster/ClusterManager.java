package com.jd.journalq.broker.cluster;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.config.BrokerConfig;
import com.jd.journalq.common.domain.*;
import com.jd.journalq.common.event.*;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.exception.JMQException;
import com.jd.journalq.common.response.BooleanResponse;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.toolkit.concurrent.EventBus;
import com.jd.journalq.toolkit.concurrent.EventListener;
import com.jd.journalq.toolkit.lang.LifeCycle;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.network.IpUtil;
import com.jd.journalq.toolkit.service.Service;
import com.jd.journalq.toolkit.time.SystemClock;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 集群管理
 * <p>
 * clusterManager提供接口都是本broker上相关的数据，如果不存在则返回null
 *
 * @author chengzhiliang on 2018/8/22.
 */
public class ClusterManager extends Service {

    private final Logger logger = LoggerFactory.getLogger(ClusterManager.class);
    //broker ID 存储目录
    private File brokerIdFile;
    //broker config
    private volatile BrokerConfig brokerConfig;
    //private volatile Integer brokerId;
    private volatile Broker broker;
    // 元数据管理
    private NameService nameService;
    // 集群管理本地缓存
    private MetaDataLocalCache localCache;
    // 元数据事件
    private EventBus<MetaEvent> eventBus = new EventBus("CLUSTER_MANAGER_EVENT");
    // K=事件, V=监听集合; 事件对于监听此事件的监听器集合
    private Map<EventType, List<EventListener<MetaEvent>>> eventListeners = new HashMap<>();

    private BrokerContext brokerContext;

    public ClusterManager(BrokerConfig brokerConfig, NameService nameService, BrokerContext brokerContext) {
        this.brokerConfig = brokerConfig;
        this.nameService = nameService;
        this.brokerContext = brokerContext;
    }


    @Override
    protected void validate() throws Exception {
        super.validate();
        Preconditions.checkArgument(brokerConfig != null, "brokerConfig can not be null.");
        Preconditions.checkArgument(nameService != null, "nameService can not be null.");

        if (brokerIdFile == null) {
            String path = brokerConfig.getBrokerIdFilePath();
            if (path != null) {
                brokerIdFile = new File(path);
            }
        }
        Preconditions.checkArgument(brokerIdFile != null, "broker ID file can not be null.");
        if (!brokerIdFile.exists()) {
            brokerIdFile.createNewFile();
        }
        if (localCache == null) {
            localCache = new MetaDataLocalCache(nameService);
        }
    }


    @Override
    protected void doStart() throws Exception {
        super.doStart();
        localCache.start();
        //TODO 刚到这里是否合适
        register();
        eventBus.start();
        localCache.initCache();
        eventBus.addListener(event -> listenEvent(event));
        logger.info("clusterManager is started");
    }

    /**
     * 注册broker
     *
     * @return
     */
    private void register() throws Exception {
        String localIp = IpUtil.getLocalIp();
        long port = brokerConfig.getFrontendConfig().getPort();
        Integer brokerId = readBroker();
        broker = nameService.register(brokerId, localIp, (int) port);
        // brokerId
        if (broker == null) {
            logger.error("brokerId[{}] [{}:{}] 注册失败", brokerId, localIp, port);
            throw new JMQException(JMQCode.CN_SERVICE_NOT_AVAILABLE);
        }
        brokerConfig.setBroker(broker);
        writeBroker(broker.getId());
    }

    /**
     * 监听元数据变更事件
     *
     * @param event
     */
    private void listenEvent(MetaEvent event) {
        try {
            readLock.lock();
            // 获取对于监听
            List<EventListener<MetaEvent>> eventListeners = this.eventListeners.get(event.getEventType());
            if (CollectionUtils.isNotEmpty(eventListeners)) {
                // 发布事件
                eventListeners.stream().forEach(listener -> eventBus.add(event, listener));
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 添加元数据变更的事件监听
     *
     * @param event    事件集合
     * @param listener 事件监听
     */
    public void addListener(EventType event, EventListener<MetaEvent> listener) {
        try {
            writeLock.lock();
            List<EventListener<MetaEvent>> eventListeners = this.eventListeners.get(event);
            if (eventListeners == null) {
                eventListeners = new ArrayList<>();
                this.eventListeners.put(event, eventListeners);
            }
            eventListeners.add(listener);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 添加元数据变更的事件监听
     *
     * @param listener 事件监听
     */
    public void addListener(EventListener<MetaEvent> listener) {
        eventBus.addListener(listener);
    }

    /**
     * 获取数据中心
     *
     * @param ipAddr
     * @return 数据中心
     */
    public DataCenter getDataCenterByIP(String ipAddr) {
        DataCenter ipInfo = nameService.getDataCenter(ipAddr);
        return ipInfo;
    }

    /**
     * 获取broker编号
     *
     * @return
     */
    public Integer getBrokerId() {
        return broker.getId();
    }

    /**
     * 获取当前broker实例config信息
     *
     * @return
     */
    public BrokerConfig getConfig() {
        return brokerConfig;
    }

    /**
     * 获取当前broker信息
     *
     * @return
     */
    public Broker getBroker() {
        return broker;
    }

    /**
     * 获取当前broker信息
     *
     * @return
     */
    public Broker getBrokerById(Integer brokerId) {
        return nameService.getBroker(brokerId);
    }

    /**
     * 根据主题+分区查询对应的主master
     *
     * @param topic     主题
     * @param partition 分区
     * @return
     */
    public Broker getBrokerByPartition(TopicName topic, short partition) {
        TopicConfig topicConfig = localCache.getTopicConfig(topic);
        return topicConfig.fetchBrokerByPartition(partition);
    }

    /**
     * 根据主题+分区查询对应的主master
     *
     * @param topic 主题
     * @param group group
     * @return
     */
    public PartitionGroup getPartitionGroupByGroup(TopicName topic, int group) {
        return getTopicConfig(topic).fetchPartitionGroupByGroup(group);
    }

    /**
     * 获取本地broker中topic信息(非该broker相关的topic不应该返回数据,实现需要根据broker.id和topic加起来获取nameservice的topicConfig)
     *
     * @param topic
     * @return
     */
    public TopicConfig getTopicConfig(TopicName topic) {
        return localCache.getTopicConfig(topic);
    }

    /**
     * 获取本地broker所有topic信息
     *
     * @return
     */
    public List<TopicConfig> getTopics() {
        return new ArrayList<>(localCache.getTopicConfigCache().values());
    }

    /**
     * 获取本地broker上该topic对应的partitionGroup
     *
     * @param topic
     * @return
     */
    public List<PartitionGroup> getPartitionGroup(TopicName topic) {
        TopicConfig topicConfig = getTopicConfig(topic);
        if (null == topicConfig) return null;
        return (List) topicConfig.fetchPartitionGroupByBrokerId(broker.getId());
    }

    /**
     * 获取本地broker上该topic对应的partitionGroup 包含replica
     *
     * @param topic
     * @return
     */
    public List<PartitionGroup> getTopicPartitionGroups(TopicName topic) {
        TopicConfig topicConfig = getTopicConfig(topic);
        if (null == topicConfig) return null;
        return topicConfig.fetchTopicPartitionGroupsByBrokerId(broker.getId());
    }

    /**
     * 获取分区对应的分区组
     *
     * @param topic
     * @param partition
     * @return
     */
    public PartitionGroup getPartitionGroup(TopicName topic, short partition) {
        TopicConfig topicConfig = getTopicConfig(topic);
        if (topicConfig != null) {
            return topicConfig.fetchPartitionGroupByPartition(partition);
        }
        return null;
    }

    /**
     * 获取分区对于分区组的ID
     *
     * @param topic
     * @param partition
     * @return
     */
    public Integer getPartitionGroupId(TopicName topic, short partition) {
        PartitionGroup partitionGroup = getPartitionGroup(topic, partition);
        Integer group = null;
        if (partitionGroup != null) {
            group = partitionGroup.getGroup();
        }
        return group;
    }

    /**
     * 获取本地broker上该topic对应的replicaGroup
     *
     * @param topic
     * @return
     */
    public List<Integer> getReplicaGroup(TopicName topic) {
        List<Integer> result = new ArrayList<>();
        List<PartitionGroup> partitionGroup = getPartitionGroup(topic);
        if (partitionGroup != null) {
            partitionGroup.stream().forEach(item ->
                    result.addAll(item.getReplicas())
            );
        }
        return result;
    }

    /**
     * 获取该topic对应的所有partition
     *
     *
     * @param topic
     * @return
     */
    public List<Short> getPartitionList(TopicName topic) {
        TopicConfig config = getTopicConfig(topic);
        return new ArrayList<>(config.fetchAllPartitions());
    }

    /**
     * 获取指定主题在broker上的角色是master的分区集合
     *
     * @param topic 主题
     * @return 分区集合
     */
    public List<Short> getMasterPartitionList(TopicName topic) {
        return localCache.getMasterPartitionsByTopic(topic);
    }

    /**
     * 获取指定主题在broker上高优先级分区集合
     *
     * @param topic 主题
     * @return 分区集合
     */
    public List<Short> getPriorityPartitionList(TopicName topic) {
        TopicConfig topicConfig = getTopicConfig(topic);
        if (topicConfig != null) {
            Set<Short> priorityPartitions = topicConfig.getPriorityPartitions();
            if (priorityPartitions != null) {
                return new ArrayList<>(priorityPartitions);
            }
        }
        return new ArrayList<>(0);
    }

    /**
     * 获取消费策略
     *
     * @param topic
     * @param app
     * @return
     */
    public Consumer.ConsumerPolicy getConsumerPolicy(TopicName topic, String app) throws JMQException {
        Consumer consumer = localCache.getConsumerByTopicAndApp(topic, app);
        if (null == consumer) throw new JMQException(JMQCode.FW_CONSUMER_NOT_EXISTS);
        return getConsumerPolicyOrDefault(consumer);
    }

    /**
     * 获取消息信息
     *
     * @param topic
     * @param app
     * @return
     */
    public Consumer getConsumer(TopicName topic, String app) throws JMQException {
        Consumer consumer = localCache.getConsumerByTopicAndApp(topic, app);
        if (null == consumer) throw new JMQException(JMQCode.FW_CONSUMER_NOT_EXISTS);
        return consumer;
    }

    /**
     * 获取消费策略
     *
     * @param topic
     * @param app
     * @return
     */
    public Consumer.ConsumerPolicy tryGetConsumerPolicy(TopicName topic, String app) {
        Consumer consumer = localCache.getConsumerByTopicAndApp(topic, app);
        if (consumer == null) {
            return null;
        }
        return getConsumerPolicyOrDefault(consumer);
    }

    /**
     * 获取生产策略
     *
     * @param topic
     * @param app
     * @return
     */
    public Producer.ProducerPolicy tryGetProducerPolicy(TopicName topic, String app) {
        Producer producer = localCache.getProducerByTopicAndApp(topic, app);
        if (producer == null) {
            return null;
        }
        return getProducerPolicyOrDefault(producer);
    }

    /**
     * 是否开启就近消费
     *
     * @param topic 主题
     * @param app   应用
     * @return
     */
    public boolean isNeedNearby(TopicName topic, String app) throws JMQException {
        return getConsumerPolicy(topic, app).getNearby();
    }

    /**
     * 判断是否配置延迟消费
     *
     * @param topic
     * @return 是否需要延迟消费
     * @Param app
     */
    public boolean isNeedDelay(TopicName topic, String app) throws JMQException {
        return getConsumerPolicy(topic, app).getDelay() > 0;
    }

    /**
     * 是否需要长轮询
     *
     * @param topic 主题
     * @return 是否长轮询
     */
    public boolean isNeedLongPull(String topic) {
        if (topic == null) {
            return false;
        }

        // 顺序消息不支持长轮询
        TopicConfig config = localCache.getTopicConfigCache().get(topic);
        return !(config != null && config.checkSequential());
    }

    /**
     * 获取应答超时时间
     *
     * @param topic 主题
     * @param app   应用
     * @return
     */
    public int getAckTimeout(TopicName topic, String app) throws JMQException {
        return getConsumerPolicy(topic, app).getAckTimeout();
    }


    /**
     * 获取生产策略
     *
     * @param topic
     * @param app
     * @return
     */
    public Producer.ProducerPolicy getProducerPolicy(TopicName topic, String app) throws JMQException {
        Producer producer = localCache.getProducerByTopicAndApp(topic, app);
        if (null == producer) throw new JMQException(JMQCode.FW_PRODUCER_NOT_EXISTS);
        return getProducerPolicyOrDefault(producer);
    }

    private Producer.ProducerPolicy getProducerPolicyOrDefault(Producer producer) {
        Producer.ProducerPolicy producerPolicy = producer.getProducerPolicy();
        if (producerPolicy == null) {
            producer.setProducerPolicy(brokerContext.getProducerPolicy());
        }
        return producer.getProducerPolicy();
    }

    private Consumer.ConsumerPolicy getConsumerPolicyOrDefault(Consumer consumer) {
        Consumer.ConsumerPolicy consumerPolicy = consumer.getConsumerPolicy();
        if (consumerPolicy == null) {
            consumer.setConsumerPolicy(brokerContext.getConsumerPolicy());
        }
        return consumer.getConsumerPolicy();
    }

    /**
     * 检查是否能生产数据
     * <br>
     * <ui>是否有PartitionGroup是master</ui>
     * <ui>有主题</ui>
     * <ui>有应用</ui>
     * <ui>是否在黑名单</ui>
     *
     * @param topic 主题
     * @param app   应用
     * @return
     */
    public BooleanResponse checkWritable(TopicName topic, String app, String address) {
        TopicConfig topicConfig = getTopicConfig(topic);
        if (topicConfig == null) {
            // 没有主题配置
            logger.error("topic[{}] app[{}] cant't be write on broker [{}],has no topicConfig", topic, app, broker.getId() + "[" + broker.getIp() + ":" + broker.getPort() + "]");
            return BooleanResponse.failed(JMQCode.FW_TOPIC_NOT_EXIST);
        }
        Producer.ProducerPolicy producerPolicy = null;
        try {
            producerPolicy = getProducerPolicy(topic, app);
        } catch (JMQException e) {
            logger.error("topic[{}],app[{}],error[{}]", topic, app, e.getMessage());
            return BooleanResponse.failed(JMQCode.valueOf(e.getCode()));
        }
        Set<String> blackList = producerPolicy != null ? producerPolicy.getBlackList() : null;
        if (blackList != null) {
            if (blackList.stream().anyMatch(ip -> ip.equals(address))) {
                // 是否在生产黑名单内
                logger.error("topic[{}] app[{}] cant't be write on broker [] in blacklist", topic, app, broker.getId() + "[" + broker.getIp() + ":" + broker.getPort() + "]");
                return BooleanResponse.failed(JMQCode.FW_PUT_MESSAGE_TOPIC_NOT_WRITE);
            }
        }
        ;
        if (logger.isDebugEnabled()) logger.debug("checkWritable topicConfig[{}]", topicConfig);
        Collection<PartitionGroup> partitionGroups = topicConfig.fetchPartitionGroupByBrokerId(broker.getId());
        if (CollectionUtils.isEmpty(partitionGroups)) {
            // 没有partitionGroup
            logger.error("topic[{}] app[{}] cant't be write on broker [{}] has no partitionGroups", topic, app, broker.getId() + "[" + broker.getIp() + ":" + broker.getPort() + "]");
            return BooleanResponse.failed(JMQCode.FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER);
        }
        if (partitionGroups.stream().noneMatch(partitionGroup -> partitionGroup.getLeader().equals(broker.getId()))) {
            logger.error("topic[{}] cant't be write on broker [] ", topic, app, broker.getId() + "[" + broker.getIp() + ":" + broker.getPort() + "]");
            return BooleanResponse.failed(JMQCode.FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER);
        }
        ;
        return BooleanResponse.success();
    }

    /**
     * 检查是否能生产数据
     * <br>
     * <ui>是否有PartitionGroup是master</ui>
     * <ui>有主题</ui>
     * <ui>有应用</ui>
     * <ui>是否在黑名单</ui>
     *
     * @param topic 主题
     * @param app   应用
     * @return
     * @Param address ip
     * @Param partition
     */
    public BooleanResponse checkWritable(TopicName topic, String app, String address, short partition) {
        BooleanResponse response = checkWritable(topic, app, address);
        if (!response.isSuccess()) return response;
        TopicConfig topicConfig = getTopicConfig(topic);
        PartitionGroup group = topicConfig.fetchPartitionGroupByPartition(partition);
        if (!group.getLeader().equals(broker.getId())) {
            logger.error("topic[{}],app[{}],error[{}]", topic, app, JMQCode.FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER.getMessage());
            return BooleanResponse.failed(JMQCode.FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER);
        }
        return BooleanResponse.success();
    }

    /**
     * 检查是否能消费数据
     * <br>
     * <ui>是否有PartitionGroup是master</ui>
     * <ui>有主题</ui>
     * <ui>有应用</ui>
     * <ui>是否停止消费</ui>
     * <ui>是否在黑名单</ui>
     *
     * @param topic 主题
     * @param app   应用
     * @return 是否可读
     */
    public BooleanResponse checkReadable(TopicName topic, String app, String address) {
        TopicConfig topicConfig = getTopicConfig(topic);
        if (topicConfig == null) {
            // 没有主题配置
            return BooleanResponse.failed(JMQCode.FW_TOPIC_NOT_EXIST);
        }
        Consumer.ConsumerPolicy consumerPolicy = null;
        try {
            consumerPolicy = getConsumerPolicy(topic, app);
        } catch (JMQException e) {
            logger.error("topic[{}],app[{}],error[{}]", topic, app, e.getMessage());
            return BooleanResponse.failed(JMQCode.valueOf(e.getCode()));
        }
        Boolean paused = consumerPolicy.getPaused();
        if (paused) {
            // 暂停消费
            return BooleanResponse.failed(JMQCode.FW_FETCH_TOPIC_MESSAGE_PAUSED);
        }
        Set<String> blackList = consumerPolicy.getBlackList();
        if (blackList != null) {
            // 是否在消费黑名单内
            if (blackList.stream().anyMatch(ip -> ip.equals(address))) {
                return BooleanResponse.failed(JMQCode.FW_GET_MESSAGE_APP_CLIENT_IP_NOT_READ);
            }
            ;
        }
        Collection<PartitionGroup> partitionGroups = topicConfig.fetchPartitionGroupByBrokerId(broker.getId());
        if (CollectionUtils.isEmpty(partitionGroups)) {
            // 没有partitionGroup
            logger.error("topic[{}],app[{}],error[{}]", topic, app, JMQCode.FW_TOPIC_NO_PARTITIONGROUP.getMessage());
            return BooleanResponse.failed(JMQCode.FW_TOPIC_NO_PARTITIONGROUP);
        }
        // 当前主题在该broker上有角色是master的分区组
        if (partitionGroups.stream().noneMatch(partitionGroup -> partitionGroup.getLeader().equals(broker.getId()))) {
            logger.error("topic[{}],app[{}],error[{}]", topic, app, JMQCode.FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER.getMessage());
            return BooleanResponse.failed(JMQCode.FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER);
        }
        ;
        return BooleanResponse.success();
    }

    /**
     * 检查是否能消费数据
     * <br>
     * <ui>是否有PartitionGroup是master</ui>
     * <ui>有主题</ui>
     * <ui>有应用</ui>
     * <ui>是否停止消费</ui>
     * <ui>是否在黑名单</ui>
     *
     * @param topic 主题
     * @param app   应用
     * @return 是否可读
     */
    public BooleanResponse checkReadable(TopicName topic, String app, String address, short partition) {
        BooleanResponse response = checkReadable(topic, app, address);
        if (!response.isSuccess()) return response;
        TopicConfig topicConfig = getTopicConfig(topic);
        PartitionGroup group = topicConfig.fetchPartitionGroupByPartition(partition);
        if (!group.getLeader().equals(broker.getId())) {
            logger.error("topic[{}],app[{}],error[{}]", topic, app, JMQCode.FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER.getMessage());
            return BooleanResponse.failed(JMQCode.FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER);
        }
        return BooleanResponse.success();
    }

    /**
     * 根据主题获取应用列表
     *
     * @param topic 主题
     * @return
     */
    public List<String> getAppByTopic(TopicName topic) {
        List<Consumer> consumerByTopic = nameService.getConsumerByTopic(topic);
        if (CollectionUtils.isEmpty(consumerByTopic)) {
            return new ArrayList<>(0);
        }
        List<String> appList = new ArrayList<>(consumerByTopic.size());
        consumerByTopic.stream().forEach(consume -> appList.add(consume.getApp()));

        return appList;
    }

    public AppToken getAppToken(String app, String token) {
        return nameService.getAppToken(app, token);
    }

    public boolean checkArchiveable(TopicName topicName) {
        Map<String, MetaDataLocalCache.CacheProducer> producers = localCache.getTopicProducers(topicName);
        if (null != producers && producers.size() > 0)
            for (Iterator<MetaDataLocalCache.CacheProducer> it = producers.values().iterator(); it.hasNext(); ) {
                if (getProducerPolicyOrDefault(it.next().getProducer()).getArchive()) return true;
            }
        Map<String, MetaDataLocalCache.CacheConsumer> consumers = localCache.getTopicConsumers(topicName);
        if (null != consumers && consumers.size() > 0)
            for (Iterator<MetaDataLocalCache.CacheConsumer> it = consumers.values().iterator(); it.hasNext(); ) {
                if (getConsumerPolicyOrDefault(it.next().getConsumer()).getArchive()) return true;
            }
        return false;
    }

    /**
     * 获取可以直连的重试broker
     *
     * @return
     */
    public List<Broker> getLocalRetryBroker() {
        List<Broker> brokers = nameService.getAllBrokers();
        List<Broker> localRetryBrokers = new LinkedList<>();
        if (brokers != null) {
            for (Broker broker : brokers) {
                if (!Broker.DEFAULT_RETRY_TYPE.equals(broker.getRetryType())) {
                    localRetryBrokers.add(broker);
                }
            }
        }

        return localRetryBrokers;
    }


    /**
     * 上报选举结果
     *
     * @param topic
     * @param partitionGroup
     * @param leaderBrokerId
     * @param isrId
     * @param termId
     */
    public void leaderReport(TopicName topic, int partitionGroup, int leaderBrokerId, Set<Integer> isrId, Integer termId) {
        nameService.leaderReport(topic, partitionGroup, leaderBrokerId, isrId, termId);
    }

    public boolean hasSubscribe(String subscribeApp, Subscription.Type subscribe) {
        return nameService.hasSubscribe(subscribeApp, subscribe);
    }

    private void writeBroker(Integer brokerId) throws Exception {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(brokerIdFile))) {
            bufferedWriter.write(brokerId.toString());
        }
    }

    private Integer readBroker() throws Exception {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(brokerIdFile))) {
            String brokerId = bufferedReader.readLine();
            if (null != brokerId && !"".equals(brokerId.trim())) return Integer.valueOf(brokerId);
            return null;
        }
    }

    public boolean doAuthorization(String app, String token) {
        Date now = Calendar.getInstance().getTime();
        AppToken appToken = nameService.getAppToken(app, token);
        return null != appToken && appToken.getEffectiveTime().before(now) && appToken.getExpirationTime().after(now);
    }

    public NameService getNameService() {
        return nameService;
    }

    /**
     * 元数据本地缓存
     * 1.本broker上所有的topic配置
     * 2.broker.id
     * <p>
     * Created by chengzhiliang on 2018/9/4.
     */
    private class MetaDataLocalCache implements LifeCycle {
        private AtomicBoolean start = new AtomicBoolean(false);
        private final NameService nameService;
        private ScheduledExecutorService timerUpdateAllExecutor;
        private long cacheTime = 60 * 1000;

        public MetaDataLocalCache(NameService nameService) {
            this.nameService = nameService;
            nameService.addListener(new MetaDataListener());
        }

        // K=主题，V=主题配置; 缓存当前broker上的所有主题配置信息
        private ConcurrentMap<String, TopicConfig> topicConfigCache = new ConcurrentHashMap<>();

        private ConcurrentMap<String, List<Short>> topicPartitionsCache = new ConcurrentHashMap<>();

        // K=主题+应用, V=消费者; 缓存当前broker上接入者->消费者
        private ConcurrentMap<String, ConcurrentHashMap<String, CacheConsumer>> consumerCache = new ConcurrentHashMap<>();
        // K=主题+应用, V=生产者; 缓存当前broker上接入者->生产者
        private ConcurrentMap<String, ConcurrentHashMap<String, CacheProducer>> producerCache = new ConcurrentHashMap<>();

        /**
         * 刷新缓存
         */
        protected void initCache() {
            buildTopicConfigCaches();
            timerUpdateAllExecutor.scheduleAtFixedRate(() -> {
                try {
                    logger.info("begin update all topicConfigs");
                    Map<TopicName, TopicConfig> topicConfigNew = nameService.getTopicConfigByBroker(brokerConfig.getBrokerId());
                    if (null != topicConfigNew && topicConfigNew.size() > 0) {
                        Map<String, TopicConfig> topicConfigOld = topicConfigCache;
                        for (Map.Entry<String, TopicConfig> entry : topicConfigOld.entrySet()) {
                            TopicName topicName = TopicName.parse(entry.getKey());
                            if (!topicConfigNew.containsKey(topicName)) {
                                topicConfigOld.remove(topicName.getFullName());
                                eventBus.add(TopicEvent.remove(topicName));
                                consumerCache.remove(topicName.getFullName());
                                producerCache.remove(topicName.getFullName());
                                topicPartitionsCache.remove(topicName.getFullName());
                            }
                        }
                        for (Map.Entry<TopicName, TopicConfig> entry : topicConfigNew.entrySet()) {
                            TopicName topicName = entry.getKey();
                            buildTopicConfigCache(entry.getValue());
                            if (!topicConfigOld.containsKey(topicName.getFullName())) {
                                eventBus.add(TopicEvent.add(topicName));
                            }
                            clearConsumerCache(topicName);
                            clearProducerCache(topicName);
                        }
                    }
                } catch (Exception e) {
                    logger.error("update all topicConfigs error", e);
                }
            }, 1, 1, TimeUnit.MINUTES);
        }

        /**
         * 构建主题对于主题配置的K-V结构
         */
        protected void buildTopicConfigCaches() {
            Map<TopicName, TopicConfig> topicConfigByBroker = nameService.getTopicConfigByBroker(brokerConfig.getBrokerId());
            if (null != topicConfigByBroker) {
                for (Map.Entry<TopicName, TopicConfig> entry : topicConfigByBroker.entrySet()) {
                    buildTopicConfigCache(entry.getValue());
                }
            }
        }

        protected TopicConfig getTopicConfig(TopicName topic) {
            TopicConfig topicConfig = topicConfigCache.get(topic.getFullName());
            if (null != topicConfig) return topicConfig;
            return buildTopicConfigCache(topic);
        }

        /**
         * 根据主题更新主题配置
         *
         * @param topic 主题
         */
        protected TopicConfig buildTopicConfigCache(TopicName topic) {
            TopicConfig topicConfig = nameService.getTopicConfig(topic);
            if (null != topicConfig) {
                return buildTopicConfigCache(topicConfig);
            }
            return null;
        }
        private TopicConfig buildTopicConfigCache(TopicConfig topicConfig) {
            TopicName topic = topicConfig.getName();
            topicConfigCache.put(topic.getFullName(), topicConfig);
            topicPartitionsCache.put(topic.getFullName(),topicConfig.fetchPartitionByBroker(broker.getId()));
            if(!consumerCache.containsKey(topic.getFullName()))consumerCache.put(topic.getFullName(),new ConcurrentHashMap<>());
            if(!producerCache.containsKey(topic.getFullName()))producerCache.put(topic.getFullName(),new ConcurrentHashMap<>());
//            nameService.getConsumerByTopic(topic).forEach(consumer -> consumerCache.get(topic.getFullName()).put(consumer.getApp(),new CacheConsuemer(consumer)));
//            nameService.getProducerByTopic(topic).forEach(producer -> producerCache.get(topic.getFullName()).put(producer.getApp(),new CacheProducer(producer)));
            return topicConfig;
        }

        /**
         * 按接入对象更新消费策略
         *
         * @param topic 主题
         * @param app   应用
         */

        protected Consumer buildConsumeCache(TopicName topic, String app) {
            Consumer consumerByTopic = nameService.getConsumerByTopicAndApp(topic, app);
            if (null != consumerByTopic) {
                consumerCache.get(topic.getFullName()).put(app, new CacheConsumer(consumerByTopic, SystemClock.now()));
            }
            return consumerByTopic;
        }

        protected Consumer getConsumerByTopicAndApp(TopicName topic, String app) {
            if (!consumerCache.containsKey(topic.getFullName())) {
                logger.warn("topic {} is not exist on this broker", topic.getFullName());
                return null;
            }
            CacheConsumer consumer = consumerCache.get(topic.getFullName()).get(app);
            if (null == consumer) {
                if (topicConfigCache.containsKey(topic.getFullName())) return buildConsumeCache(topic, app);
            }
            return consumer.getConsumer();
        }

        protected Producer getProducerByTopicAndApp(TopicName topic, String app) {
            if (!producerCache.containsKey(topic.getFullName())) {
                logger.warn("topic {} is not exist on this broker", topic.getFullName());
                return null;
            }
            CacheProducer producer = producerCache.get(topic.getFullName()).get(app);
            if (null == producer) {
                if (topicConfigCache.containsKey(topic.getFullName())) return buildProduceCache(topic, app);
            }
            return producer.getProducer();
        }

        protected List<Short> getMasterPartitionsByTopic(TopicName topic) {
            List<Short> partitions = topicPartitionsCache.get(topic.getFullName());
            if (null != partitions)
                return partitions;
            TopicConfig topicConfig = getTopicConfig(topic);
            topicPartitionsCache.put(topic.getFullName(), topicConfig.fetchPartitionByBroker(broker.getId()));
            return topicPartitionsCache.get(topic.getFullName());
        }

        /**
         * 按接入对象更新生产策略
         *
         * @param topic 主题
         * @param app   应用
         */
        protected Producer buildProduceCache(TopicName topic, String app) {
            Producer producerByTopic = nameService.getProducerByTopicAndApp(topic, app);
            if (null != producerByTopic) {
                producerCache.get(topic.getFullName()).put(app, new CacheProducer(producerByTopic));
            }
            return producerByTopic;
        }


        public ConcurrentMap<String, TopicConfig> getTopicConfigCache() {
            return topicConfigCache;
        }

        public Map<String, CacheConsumer> getTopicConsumers(TopicName topic) {
            return consumerCache.get(topic.getFullName());
        }

        public Map<String, CacheProducer> getTopicProducers(TopicName topic) {
            return producerCache.get(topic.getFullName());
        }

        private void clearConsumerCache(TopicName topicName) {
            Map<String, CacheConsumer> cacheConsuemerMap = consumerCache.get(topicName.getFullName());
            cacheConsuemerMap.values().forEach(cacheConsuemer -> {
                if (SystemClock.now() > cacheConsuemer.getExpireTime()) {
                    cacheConsuemerMap.remove(cacheConsuemer.getConsumer().getApp());
                }
            });
        }

        private void clearProducerCache(TopicName topicName) {
            Map<String, CacheProducer> cacheProducerMap = producerCache.get(topicName.getFullName());
            cacheProducerMap.values().forEach(cacheProducer -> {
                if (SystemClock.now() > cacheProducer.getExpireTime()) {
                    cacheProducerMap.remove(cacheProducer.getProducer().getApp());
                }
            });
        }

        @Override
        public void start() throws Exception {
            timerUpdateAllExecutor = Executors.newSingleThreadScheduledExecutor();
            start.compareAndSet(false, true);
        }

        @Override
        public void stop() {
            timerUpdateAllExecutor.shutdown();
            start.compareAndSet(true, false);
        }

        @Override
        public boolean isStarted() {
            return start.get();
        }

        /**
         * 主题信息更新事件处理
         */
        class MetaDataListener implements EventListener<NameServerEvent> {

            @Override
            public void onEvent(NameServerEvent event) {
                try {
                    if (!brokerConfig.getBrokerId().equals(event.getBrokerId())) return;
                    logger.info("event[{}]", event);
                    /**
                     * 新添topic，通知所有的partitionGroup
                     * 1.
                     */
                    if (event.getMetaEvent() instanceof TopicEvent) {
                        switch (event.getEventType()) {
                            case ADD_TOPIC:
                                TopicConfig topicConfig = buildTopicConfigCache(((TopicEvent) event.getMetaEvent()).getTopic());
                                for (PartitionGroup group : topicConfig.fetchPartitionGroupByBrokerId(brokerConfig.getBrokerId())) {
                                    //storeService.createPartitionGroup(group.getTopic(), group.getGroup(), group.getPartitions().toArray(new Short[0]),group.getReplicaGroups().stream().mapToInt(replica->(int)replica).toArray());
                                    eventBus.add(PartitionGroupEvent.add(group.getTopic(), group.getGroup()));
                                }
                                break;
                            case UPDATE_TOPIC:
                                // 主题新增/更新事件
                                buildTopicConfigCache(((TopicEvent) event.getMetaEvent()).getTopic());
                                break;
                            case REMOVE_TOPIC:
                                TopicConfig topicConfigRemove = topicConfigCache.remove(((TopicEvent) event.getMetaEvent()).getTopic().getFullName());
                                consumerCache.remove(topicConfigRemove.getName().getFullName());
                                producerCache.remove(topicConfigRemove.getName().getFullName());
                                for (PartitionGroup group : topicConfigRemove.fetchPartitionGroupByBrokerId(brokerConfig.getBrokerId())) {
                                    //storeService.removePartitionGroup(group.getTopic(), group.getGroup());
                                    eventBus.add(PartitionGroupEvent.remove(group.getTopic(), group.getGroup()));
                                }
                                break;
                        }
                    } else if (event.getMetaEvent() instanceof PartitionGroupEvent) {
                        TopicConfig topicConfig = buildTopicConfigCache(((PartitionGroupEvent) event.getMetaEvent()).getTopic());
                        PartitionGroup group = topicConfig.fetchPartitionGroupByGroup(((PartitionGroupEvent) event.getMetaEvent()).getPartitionGroup());
                        switch (event.getEventType()) {
                            case ADD_PARTITION_GROUP:
                                //PartitionGroup新增事件
                                //storeService.createPartitionGroup(group.getTopic(), group.getGroup(), group.getPartitions().toArray(new Short[0]),group.getReplicaGroups().stream().mapToInt(replica->(int)replica).toArray());
                                eventBus.add(PartitionGroupEvent.add(topicConfig.getName(), group.getGroup()));
                                break;
                            case UPDATE_PARTITION_GROUP:
                                // PartitionGroup更新事件,只会通知partitionGroup的leader
                                //storeService.createOrUpdatePartitionGroup(group.getTopic(), group.getGroup(), (Short[]) group.getPartitions().toArray());
                                eventBus.add(PartitionGroupEvent.update(group.getTopic(), group.getGroup()));
                                break;
                            case REMOVE_PARTITION_GROUP:
                                /**
                                 * 删除partitionGroup时候通知到所有相关的broker
                                 */
                                //storeService.removePartitionGroup(group.getTopic(), group.getGroup());
                                eventBus.add(PartitionGroupEvent.remove(topicConfig.getName(), ((PartitionGroupEvent) event.getMetaEvent()).getPartitionGroup()));
                                break;
                        }
                    } else {
                        switch (event.getMetaEvent().getEventType()) {
                            case ADD_CONSUMER:
                            case UPDATE_CONSUMER:
                                //consumer update add
                                ConsumerEvent consumerEvent = (ConsumerEvent) event.getMetaEvent();
                                buildConsumeCache(consumerEvent.getTopic(), consumerEvent.getApp());
                                break;
                            case REMOVE_CONSUMER:
                                ConsumerEvent removeConsumerEvent = (ConsumerEvent) event.getMetaEvent();
                                consumerCache.get(removeConsumerEvent.getTopic().getFullName()).remove(removeConsumerEvent.getApp());
                                break;
                            case ADD_PRODUCER:
                            case UPDATE_PRODUCER:
                                // 主题新增事件
                                ProducerEvent producerEvent = (ProducerEvent) event.getMetaEvent();
                                buildProduceCache(producerEvent.getTopic(), producerEvent.getApp());
                                break;
                            case REMOVE_PRODUCER:
                                ProducerEvent removeProducerEvent = (ProducerEvent) event.getMetaEvent();
                                producerCache.get(removeProducerEvent.getTopic()).remove(removeProducerEvent.getApp());
                                break;
                        }
                        eventBus.add(event);
                    }
                } catch (Exception e) {
                    logger.error(String.format("clusterManager event[%s] error", event), e);
                }
            }
        }

        private class CacheConsumer {
            private Consumer consumer;
            private long expireTime;


            public CacheConsumer(Consumer consumer) {
                this.consumer = consumer;
                this.expireTime = SystemClock.now() + cacheTime;
            }

            public CacheConsumer(Consumer consumer, long expireTime) {
                this.consumer = consumer;
                this.expireTime = expireTime;
            }

            public Consumer getConsumer() {
                return consumer;
            }

            public long getExpireTime() {
                return expireTime;
            }
        }

        private class CacheProducer {
            private Producer producer;
            private long expireTime;

            public CacheProducer(Producer producer) {
                this.producer = producer;
                this.expireTime = SystemClock.now() + cacheTime;
            }

            public CacheProducer(Producer producer, long expireTime) {
                this.producer = producer;
                this.expireTime = expireTime;
            }

            public Producer getProducer() {
                return producer;
            }

            public long getExpireTime() {
                return expireTime;
            }
        }
    }

    @Override
    protected void doStop() {
        super.doStop();
        if (eventBus.isStarted()) eventBus.stop();
        if (localCache.isStarted()) localCache.stop();
        logger.info("clusterManager is stopped");
    }
}