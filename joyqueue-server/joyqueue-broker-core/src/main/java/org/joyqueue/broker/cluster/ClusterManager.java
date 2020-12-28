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
package org.joyqueue.broker.cluster;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.cluster.config.ClusterConfig;
import org.joyqueue.broker.cluster.entry.ClusterNode;
import org.joyqueue.broker.config.BrokerConfig;
import org.joyqueue.broker.consumer.ConsumeConfigKey;
import org.joyqueue.domain.AppToken;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.DataCenter;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.Subscription;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.event.NameServerEvent;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.nsr.NameService;
import org.joyqueue.nsr.event.UpdateBrokerEvent;
import org.joyqueue.response.BooleanResponse;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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

    private BrokerContext brokerContext;
    private ClusterNameService clusterNameService;
    private ClusterConfig config;

    private Cache<TopicName, List<Short>> localPartitionCache;

    public ClusterManager(BrokerConfig brokerConfig, NameService nameService, ClusterNameService clusterNameService, BrokerContext brokerContext) {
        this.brokerConfig = brokerConfig;
        this.nameService = nameService;
        this.clusterNameService = clusterNameService;
        this.brokerContext = brokerContext;
        this.config = new ClusterConfig(brokerContext.getPropertySupplier());
        this.localPartitionCache = CacheBuilder.newBuilder().expireAfterWrite(1000 * 1, TimeUnit.MILLISECONDS).build();
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
    }


    @Override
    protected void doStart() throws Exception {
        super.doStart();
        register();
        clusterNameService.setBroker(broker);
        nameService.addListener(new MetaDataListener());
        logger.info("clusterManager is started");
    }

    /**
     * 注册broker
     *
     * @return
     */
    private void register() throws Exception {
        String localIp = brokerConfig.getFrontendConfig().getHost();
        long port = brokerConfig.getFrontendConfig().getPort();
        Integer brokerId = readBroker();
        broker = nameService.register(brokerId, localIp, (int) port);
        // brokerId
        if (broker == null) {
            logger.error("brokerId[{}] [{}:{}] 注册失败", brokerId, localIp, port);
            throw new JoyQueueException(JoyQueueCode.CN_SERVICE_NOT_AVAILABLE);
        }
        brokerConfig.setBroker(broker);
        writeBroker(broker.getId());
    }

    /**
     * 添加元数据变更的事件监听
     *
     * @param listener 事件监听
     */
    public void addListener(EventListener<MetaEvent> listener) {
        nameService.addListener(new EventListener<NameServerEvent>() {
            @Override
            public void onEvent(NameServerEvent event) {
                listener.onEvent(event.getMetaEvent());
            }
        });
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
        if (brokerId.equals(getBrokerId())) {
            return broker;
        }
        return nameService.getBroker(brokerId);
    }

    /**
     * 根据主题+分区查询对应的主master
     *
     * @param topic 主题
     * @param group group
     * @return
     */
    public PartitionGroup getPartitionGroupByGroup(TopicName topic, int group) {
        TopicConfig topicConfig = getTopicConfig(topic);
        if (topicConfig == null) {
            return null;
        }
        return topicConfig.fetchPartitionGroupByGroup(group);
    }

    /**
     * 获取本地broker中topic信息(非该broker相关的topic不应该返回数据,实现需要根据broker.id和topic加起来获取nameservice的topicConfig)
     *
     * @param topic
     * @return
     */
    public TopicConfig getTopicConfig(TopicName topic) {
        TopicConfig topicConfig = nameService.getTopicConfig(topic);
        if (topicConfig == null) {
            return null;
        }
        if (!topicConfig.isReplica(getBrokerId())) {
            return null;
        }
        return topicConfig;
    }

    /**
     * 获取本地broker所有topic信息
     *
     * @return
     */
    public List<TopicConfig> getTopics() {
        Map<TopicName, TopicConfig> topics = nameService.getTopicConfigByBroker(getBrokerId());
        if (MapUtils.isEmpty(topics)) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(topics.values());
    }

    /**
     * 获取本地broker上该topic对应的partitionGroup
     *
     * @param topic
     * @return
     */
    public List<PartitionGroup> getLocalPartitionGroups(TopicName topic) {
        TopicConfig topicConfig = getTopicConfig(topic);
        if (null == topicConfig) {
            return null;
        }
        return getLocalPartitionGroups(topicConfig);
    }

    public List<PartitionGroup> getLocalPartitionGroups(TopicConfig topicConfig) {
        List<PartitionGroup> result = Lists.newArrayListWithCapacity(topicConfig.getPartitionGroups().size());
        for (Map.Entry<Integer, PartitionGroup> entry : topicConfig.getPartitionGroups().entrySet()) {
            if (isLeader(entry.getValue())) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    public List<Short> getLocalPartitions(TopicName topic) {
        try {
            return localPartitionCache.get(topic, () -> {
                TopicConfig topicConfig = getTopicConfig(topic);
                if (topicConfig == null) {
                    return Collections.emptyList();
                }
                List<Short> result = Lists.newArrayListWithCapacity(topicConfig.getPartitions());
                for (Map.Entry<Integer, PartitionGroup> entry : topicConfig.getPartitionGroups().entrySet()) {
                    if (isLeader(entry.getValue())) {
                        result.addAll(entry.getValue().getPartitions());
                    }
                }
                return result;
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Short> getLocalPartitions(TopicConfig topicConfig) {
        try {
            return localPartitionCache.get(topicConfig.getName(), () -> {
                List<Short> result = Lists.newArrayListWithCapacity(topicConfig.getPartitions());
                for (Map.Entry<Integer, PartitionGroup> entry : topicConfig.getPartitionGroups().entrySet()) {
                    if (isLeader(entry.getValue())) {
                        result.addAll(entry.getValue().getPartitions());
                    }
                }
                return result;
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取本地broker上该topic对应的partitionGroup 包含replica
     *
     * @param topic
     * @return
     */
    public List<PartitionGroup> getTopicPartitionGroups(TopicName topic) {
        TopicConfig topicConfig = getTopicConfig(topic);
        if (null == topicConfig) {
            return null;
        }
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
    public List<Integer> getReplicaGroups(TopicName topic) {
        List<Integer> result = new ArrayList<>();
        List<PartitionGroup> partitionGroup = getLocalPartitionGroups(topic);
        if (partitionGroup != null) {
            partitionGroup.stream().forEach(item ->
                    result.addAll(item.getReplicas())
            );
        }
        return result;
    }

    public List<Short> getReplicaPartitions(TopicName topic) {
        TopicConfig topicConfig = getTopicConfig(topic);
        if (topicConfig == null) {
            return Collections.emptyList();
        }
        List<Short> result = Lists.newArrayListWithCapacity(topicConfig.getPartitions());
        for (Map.Entry<Integer, PartitionGroup> entry : topicConfig.getPartitionGroups().entrySet()) {
            if (entry.getValue().getReplicas().contains(getBrokerId())) {
                result.addAll(entry.getValue().getPartitions());
            }
        }
        return result;
    }

    /**
     * 获取该topic对应的所有partition
     *
     * @param topic
     * @return
     */
    public List<Short> getPartitionList(TopicName topic) {
        TopicConfig config = getTopicConfig(topic);
        if (config != null) {
            return new ArrayList<>(config.fetchAllPartitions());
        } else {
            return Collections.emptyList();
        }
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
        return Collections.emptyList();
    }

    /**
     * 获取消费策略
     *
     * @param topic
     * @param app
     * @return
     */
    public Consumer.ConsumerPolicy getConsumerPolicy(TopicName topic, String app) throws JoyQueueException {
        Consumer consumer = tryGetConsumer(topic, app);
        if (null == consumer) {
            if (StringUtils.equals(brokerConfig.getAdminUser(), app)) {
                return brokerContext.getConsumerPolicy();
            }
            throw new JoyQueueException(JoyQueueCode.FW_CONSUMER_NOT_EXISTS);
        }
        return getConsumerPolicyOrDefault(consumer);
    }

    public PropertySupplier getPropertySupplier() {
        return brokerContext.getPropertySupplier();
    }

    public int getRetryRandomBound(String topic, String app) {
        int topicRandomBound = doGetTopicRetryRandomBound(topic);
        if (topicRandomBound != -1) {
            return topicRandomBound;
        }
        int appRandomBound = doGetAppRetryRandomBound(app);
        if (appRandomBound != -1) {
            return appRandomBound;
        }
        return PropertySupplier.getValue(brokerContext.getPropertySupplier(), ConsumeConfigKey.RETRY_RANDOM_BOUND);
    }

    protected int doGetTopicRetryRandomBound(String topic) {
        return PropertySupplier.getValue(brokerContext.getPropertySupplier(),
                ConsumeConfigKey.RETRY_RANDOM_BOUND_TOPIC_PREFIX.getName() + topic,
                ConsumeConfigKey.RETRY_RANDOM_BOUND_TOPIC_PREFIX.getType(),
                ConsumeConfigKey.RETRY_RANDOM_BOUND_TOPIC_PREFIX.getValue());
    }

    protected int doGetAppRetryRandomBound(String app) {
        return PropertySupplier.getValue(brokerContext.getPropertySupplier(),
                ConsumeConfigKey.RETRY_RANDOM_BOUND_APP_PREFIX.getName() + app,
                ConsumeConfigKey.RETRY_RANDOM_BOUND_APP_PREFIX.getType(),
                ConsumeConfigKey.RETRY_RANDOM_BOUND_APP_PREFIX.getValue());
    }

    /**
     * 获取消息信息
     *
     * @param topic
     * @param app
     * @return
     */
    public Consumer getConsumer(TopicName topic, String app) throws JoyQueueException {
        Consumer consumer = tryGetConsumer(topic, app);
        if (null == consumer) {
            throw new JoyQueueException(JoyQueueCode.FW_CONSUMER_NOT_EXISTS);
        }
        return consumer;
    }

    public Consumer tryGetConsumer(TopicName topic, String app) {
        TopicConfig topicConfig = getTopicConfig(topic);
        if (topicConfig == null) {
            return null;
        }
        return nameService.getConsumerByTopicAndApp(topic, app);
    }

    /**
     * 获取消费策略
     *
     * @param topic
     * @param app
     * @return
     */
    public Consumer.ConsumerPolicy tryGetConsumerPolicy(TopicName topic, String app) {
        Consumer consumer = tryGetConsumer(topic, app);
        if (consumer == null) {
            if (StringUtils.equals(brokerConfig.getAdminUser(), app)) {
                return brokerContext.getConsumerPolicy();
            }
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
        Producer producer = tryGetProducer(topic, app);
        if (producer == null) {
            if (StringUtils.equals(brokerConfig.getAdminUser(), app)) {
                return brokerContext.getProducerPolicy();
            }
            return null;
        }
        return getProducerPolicyOrDefault(producer);
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
        TopicConfig config = getTopicConfig(TopicName.parse(topic));
        return !(config != null && config.checkSequential());
    }

    public Producer getProducer(TopicName topic, String app) throws JoyQueueException {
        Producer producer = tryGetProducer(topic, app);
        if (null == producer) {
            throw new JoyQueueException(JoyQueueCode.FW_PRODUCER_NOT_EXISTS);
        }
        return producer;
    }

    public Producer tryGetProducer(TopicName topic, String app) {
        TopicConfig topicConfig = getTopicConfig(topic);
        if (topicConfig == null) {
            return null;
        }
        return nameService.getProducerByTopicAndApp(topic, app);
    }

    /**
     * 获取生产策略
     *
     * @param topic
     * @param app
     * @return
     */
    public Producer.ProducerPolicy getProducerPolicy(TopicName topic, String app) throws JoyQueueException {
        Producer producer = tryGetProducer(topic, app);
        if (null == producer) {
            if (StringUtils.equals(brokerConfig.getAdminUser(), app)) {
                return brokerContext.getProducerPolicy();
            }
            throw new JoyQueueException(JoyQueueCode.FW_PRODUCER_NOT_EXISTS);
        }
        return getProducerPolicyOrDefault(producer);
    }

    private Producer.ProducerPolicy getProducerPolicyOrDefault(Producer producer) {
        Producer.ProducerPolicy producerPolicy = producer.getProducerPolicy();
        if (producerPolicy == null) {
            return brokerContext.getProducerPolicy();
        }
        return producer.getProducerPolicy();
    }

    private Consumer.ConsumerPolicy getConsumerPolicyOrDefault(Consumer consumer) {
        Consumer.ConsumerPolicy consumerPolicy = consumer.getConsumerPolicy();
        if (consumerPolicy == null) {
            return brokerContext.getConsumerPolicy();
        }
        return consumer.getConsumerPolicy();
    }

    public boolean isLeader(String topic, short partition) {
        return isLeader(TopicName.parse(topic), partition);
    }

    public boolean isLeader(String topic, int partitionGroup) {
        return isLeader(TopicName.parse(topic), partitionGroup);
    }


    public boolean isLeader(TopicName topic, int partitionGroupId) {
        TopicConfig topicConfig = getTopicConfig(topic);
        if (topicConfig == null) {
            return false;
        }
        PartitionGroup partitionGroup = topicConfig.fetchPartitionGroupByGroup(partitionGroupId);
        return (partitionGroup != null && isLeader(partitionGroup));
    }

    public boolean isLeader(TopicName topic, short partition) {
        TopicConfig topicConfig = getTopicConfig(topic);
        if (topicConfig == null) {
            return false;
        }
        PartitionGroup partitionGroup = topicConfig.fetchPartitionGroupByPartition(partition);
        return (partitionGroup != null && isLeader(partitionGroup));
    }

    public boolean isLeader(PartitionGroup partitionGroup) {
        if (!partitionGroup.getReplicas().contains(getBrokerId())) {
            return false;
        }
        if (!config.getTopicLocalElectionEnable() || partitionGroup.getElectType().equals(PartitionGroup.ElectType.fix)) {
            return getBrokerId().equals(partitionGroup.getLeader());
        }
        ClusterNode clusterNode = clusterNameService.getTopicGroupNode(partitionGroup.getTopic(), partitionGroup.getGroup());
        if (clusterNode == null) {
            return false;
        }
        return (clusterNode.getLeader() == getBrokerId());
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
        // 检查broker写权限
        BooleanResponse brokerWritable = checkBrokerWritable();
        if (!brokerWritable.isSuccess()) {
            // 当前broker没有写权限
            return brokerWritable;
        }

        TopicConfig topicConfig = getTopicConfig(topic);
        if (topicConfig == null) {
            // 没有主题配置
            logger.error("topic[{}] app[{}] cant't be write on broker [{}],has no topicConfig", topic, app, broker.getId() + "[" + broker.getIp() + ":" + broker.getPort() + "]");
            return BooleanResponse.failed(JoyQueueCode.FW_TOPIC_NOT_EXIST);
        }
        Producer.ProducerPolicy producerPolicy = null;
        try {
            producerPolicy = getProducerPolicy(topic, app);
        } catch (JoyQueueException e) {
            logger.error("topic[{}],app[{}],error[{}]", topic, app, e.getMessage());
            return BooleanResponse.failed(JoyQueueCode.valueOf(e.getCode()));
        }
        Set<String> blackList = producerPolicy != null ? producerPolicy.getBlackList() : null;
        if (blackList != null) {
            if (blackList.stream().anyMatch(ip -> ip.trim().equals(address))) {
                // 是否在生产黑名单内
                logger.error("topic[{}] app[{}] cant't be write on broker [] in blacklist", topic, app, broker.getId() + "[" + broker.getIp() + ":" + broker.getPort() + "]");
                return BooleanResponse.failed(JoyQueueCode.FW_PUT_MESSAGE_TOPIC_NOT_WRITE);
            }
        }
        Collection<PartitionGroup> partitionGroups = topicConfig.fetchTopicPartitionGroupsByBrokerId(broker.getId());
        if (partitionGroups.stream().noneMatch(partitionGroup -> isLeader(partitionGroup))) {
            logger.error("topic[{}] cant't be write on broker [] ", topic, app, broker.getId() + "[" + broker.getIp() + ":" + broker.getPort() + "]");
            return BooleanResponse.failed(JoyQueueCode.FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER);
        }
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
        if (!response.isSuccess()) {
            return response;
        }
        TopicConfig topicConfig = getTopicConfig(topic);
        PartitionGroup group = topicConfig.fetchPartitionGroupByPartition(partition);
        if (group == null || !isLeader(group)) {
            logger.error("topic[{}],app[{}],partition[{}],error[{}]", topic, app,partition, JoyQueueCode.FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER.getMessage());
            return BooleanResponse.failed(JoyQueueCode.FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER);
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
        // 检查broker读权限
        BooleanResponse brokerReadable = checkBrokerReadable();
        if (!brokerReadable.isSuccess()) {
            // 没有读权限
            return brokerReadable;
        }

        TopicConfig topicConfig = getTopicConfig(topic);
        if (topicConfig == null) {
            // 没有主题配置
            return BooleanResponse.failed(JoyQueueCode.FW_TOPIC_NOT_EXIST);
        }
        Consumer.ConsumerPolicy consumerPolicy = null;
        try {
            consumerPolicy = getConsumerPolicy(topic, app);
        } catch (JoyQueueException e) {
            logger.error("topic[{}],app[{}],error[{}]", topic, app, e.getMessage());
            return BooleanResponse.failed(JoyQueueCode.valueOf(e.getCode()));
        }
        Boolean paused = consumerPolicy.getPaused();
        if (paused) {
            // 暂停消费
            logger.info("topic is paused, topic: {}, app: {}", topic, app);
            return BooleanResponse.failed(JoyQueueCode.FW_FETCH_TOPIC_MESSAGE_PAUSED);
        }
        Set<String> blackList = consumerPolicy.getBlackList();
        if (blackList != null) {
            // 是否在消费黑名单内
            if (blackList.stream().anyMatch(ip -> ip.trim().equals(address))) {
                logger.info("app client ip not readable, topic: {}, app: {}, ip: {}", topic, app, address);
                return BooleanResponse.failed(JoyQueueCode.FW_GET_MESSAGE_APP_CLIENT_IP_NOT_READ);
            }
        }
        Collection<PartitionGroup> partitionGroups = topicConfig.fetchTopicPartitionGroupsByBrokerId(broker.getId());
        // 当前主题在该broker上有角色是master的分区组
        if (partitionGroups.stream().noneMatch(partitionGroup -> isLeader(partitionGroup))) {
            logger.error("topic[{}],app[{}],error[{}]", topic, app, JoyQueueCode.FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER.getMessage());
            return BooleanResponse.failed(JoyQueueCode.FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER);
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
    public BooleanResponse checkReadable(TopicName topic, String app, String address, short partition) {
        BooleanResponse response = checkReadable(topic, app, address);
        if (!response.isSuccess()) {
            return response;
        }
        TopicConfig topicConfig = getTopicConfig(topic);
        PartitionGroup group = topicConfig.fetchPartitionGroupByPartition(partition);
        if (group == null || !isLeader(group)) {
            logger.error("topic[{}],app[{}],partition[{}],error[{}]", topic, app,partition, JoyQueueCode.FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER.getMessage());
            return BooleanResponse.failed(JoyQueueCode.FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER);
        }
        return BooleanResponse.success();
    }

    /**
     * 检查broker级别是否读
     *
     * @return broker是否可读
     */
    public BooleanResponse checkBrokerReadable(){
        Broker broker = getBroker();
        if (Broker.PermissionEnum.FULL != broker.getPermission() && Broker.PermissionEnum.READ != broker.getPermission()) {
            logger.error("No read permission broker:[{}]", broker);

            return BooleanResponse.failed(JoyQueueCode.FW_BROKER_NOT_READABLE);
        }
        return BooleanResponse.success();
    }

    /**
     * 检查broker级别是否写
     *
     * @return broker是否可写
     */
    public BooleanResponse checkBrokerWritable(){
        Broker broker = getBroker();
        if (Broker.PermissionEnum.FULL != broker.getPermission() && Broker.PermissionEnum.WRITE != broker.getPermission()) {
            logger.error("No write permission broker info is [{}]", broker);

            return BooleanResponse.failed(JoyQueueCode.FW_BROKER_NOT_WRITABLE);
        }
        return BooleanResponse.success();
    }

    /**
     * 根据主题获取应用列表
     *
     * @param topic 主题
     * @return
     */
    public List<String> getConsumersByTopic(TopicName topic) {
        List<Consumer> consumerByTopic = nameService.getConsumerByTopic(topic);
        if (CollectionUtils.isEmpty(consumerByTopic)) {
            return Collections.emptyList();
        }
        List<String> appList = new ArrayList<>(consumerByTopic.size());
        consumerByTopic.stream().forEach(consume -> appList.add(consume.getApp()));

        return appList;
    }

    /**
     * 获取订阅了当前broker上topic的app
     *
     * @param topic
     * @return
     */
    public List<String> getLocalSubscribeAppByTopic(TopicName topic) {
        List<Consumer> consumers = nameService.getConsumerByTopic(topic);
        List<String> result = Lists.newArrayListWithCapacity(consumers.size());

        for (Consumer consumer : consumers) {
            result.add(consumer.getApp());
        }

        return result;
    }

    public List<Producer> getLocalProducersByTopic(TopicName topic) {
        return Lists.newArrayList(nameService.getProducerByTopic(topic));
    }

    public List<Consumer> getLocalConsumersByTopic(TopicName topic) {
        return Lists.newArrayList(nameService.getConsumerByTopic(topic));
    }

    public AppToken getAppToken(String app, String token) {
        return nameService.getAppToken(app, token);
    }

    public boolean checkArchiveable(TopicName topicName) {
        for (Producer producer : nameService.getProducerByTopic(topicName)) {
            if (getProducerPolicyOrDefault(producer).getArchive()) {
                return true;
            }
        }

        for (Consumer consumer : nameService.getConsumerByTopic(topicName)) {
            if (getConsumerPolicyOrDefault(consumer).getArchive()) {
                return true;
            }
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
        List<Broker> localRetryBrokers = Lists.newLinkedList();
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
            if (null != brokerId && !"".equals(brokerId.trim())) {
                return Integer.valueOf(brokerId);
            }
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

    class MetaDataListener implements EventListener<NameServerEvent> {

        @Override
        public void onEvent(NameServerEvent event) {
            if (logger.isDebugEnabled()) {
                logger.debug("onEvent, event: {}", JSON.toJSONString(event));
            }
            switch (event.getEventType()) {
                case UPDATE_BROKER: {
                    UpdateBrokerEvent updateBrokerEvent = (UpdateBrokerEvent) event.getMetaEvent();
                    if (broker != null) {
                        broker.setPermission(updateBrokerEvent.getNewBroker().getPermission());
                        broker.setRetryType(updateBrokerEvent.getNewBroker().getRetryType());
                    }
                    break;
                }
            }
        }
    }
}
