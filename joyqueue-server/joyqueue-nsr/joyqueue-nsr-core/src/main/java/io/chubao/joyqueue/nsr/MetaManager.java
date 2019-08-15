package io.chubao.joyqueue.nsr;

import com.google.common.base.Preconditions;
import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.domain.Topic;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.ConsumerEvent;
import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.event.PartitionGroupEvent;
import io.chubao.joyqueue.event.ProducerEvent;
import io.chubao.joyqueue.nsr.message.MessageListener;
import io.chubao.joyqueue.nsr.message.Messenger;
import io.chubao.joyqueue.nsr.service.AppTokenService;
import io.chubao.joyqueue.nsr.service.BrokerService;
import io.chubao.joyqueue.nsr.service.ConfigService;
import io.chubao.joyqueue.nsr.service.ConsumerService;
import io.chubao.joyqueue.nsr.service.DataCenterService;
import io.chubao.joyqueue.nsr.service.PartitionGroupReplicaService;
import io.chubao.joyqueue.nsr.service.PartitionGroupService;
import io.chubao.joyqueue.nsr.service.ProducerService;
import io.chubao.joyqueue.nsr.service.TopicService;
import io.chubao.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * meta manager
 *
 * @author lixiaobin6
 * @date 下午4:33 2018/7/25
 */
public class MetaManager extends Service {
    private static final Logger logger = LoggerFactory.getLogger(MetaManager.class);
    private TopicService topicService;
    private BrokerService brokerService;
    private ConsumerService consumerService;
    private ProducerService producerService;
    private PartitionGroupService partitionGroupService;
    private PartitionGroupReplicaService partitionGroupReplicaService;
    private ConfigService configService;
    private DataCenterService dataCenterService;
    private AppTokenService appTokenService;
    private Messenger<MetaEvent> metaMessenger;

    /**
     * construct
     *
     * @param messenger
     * @param configService
     * @param topicService
     * @param brokerService
     * @param consumerService
     * @param producerService
     * @param partitionGroupService
     * @param partitionGroupReplicaService
     * @param appTokenService
     * @param dataCenterService
     */
    public MetaManager(Messenger messenger,
                       ConfigService configService,
                       TopicService topicService,
                       BrokerService brokerService,
                       ConsumerService consumerService,
                       ProducerService producerService,
                       PartitionGroupService partitionGroupService,
                       PartitionGroupReplicaService partitionGroupReplicaService,
                       AppTokenService appTokenService,
                       DataCenterService dataCenterService) {

        Preconditions.checkArgument(messenger != null, "messenger can not be null");
        Preconditions.checkArgument(topicService != null, "topic service can not be null");
        Preconditions.checkArgument(brokerService != null, "broker service can not be null");
        Preconditions.checkArgument(consumerService != null, "consumer service can not be null");
        Preconditions.checkArgument(producerService != null, "producer service can not be null");
        Preconditions.checkArgument(dataCenterService != null, "data center service can not be null");
        Preconditions.checkArgument(partitionGroupService != null, "partition group service can not be null");
        Preconditions.checkArgument(partitionGroupReplicaService != null, "partition group replica service can not be null");
        this.topicService = topicService;
        this.metaMessenger = messenger;
        this.brokerService = brokerService;
        this.configService = configService;
        this.consumerService = consumerService;
        this.producerService = producerService;
        this.appTokenService = appTokenService;
        this.dataCenterService = dataCenterService;
        this.partitionGroupService = partitionGroupService;
        this.partitionGroupReplicaService = partitionGroupReplicaService;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        logger.info("metadata manager is started");
    }

    @Override
    protected void doStop() {
        super.doStop();
        logger.info("metadata manager is stopped");
    }

    /**
     * get topic
     *
     * @param topic
     * @return
     */
    public Topic getTopicByName(TopicName topic) {
        return topicService.getById(topic.getFullName());
    }

    /**
     * get broker
     *
     * @param brokerId
     * @return
     */
    public Broker getBrokerById(Integer brokerId) {
        return brokerService.getById(brokerId);
    }

    /**
     * get broker
     *
     * @param brokerIp
     * @param brokerPort
     * @return
     */
    public Broker getBrokerByIpAndPort(String brokerIp, Integer brokerPort) {
        return brokerService.getByIpAndPort(brokerIp, brokerPort);
    }

    /**
     * add broker
     *
     * @param broker
     * @return
     */
    public boolean addBroker(Broker broker) {
        brokerService.addOrUpdate(broker);
        return true;
    }

    /**
     * add consumer
     *
     * @param consumer
     * @return
     */
    public Consumer addConsumer(Consumer consumer) {
        consumerService.addOrUpdate(consumer);
        metaMessenger.publish(ConsumerEvent.add(consumer.getTopic(), consumer.getApp()));
        return consumer;
    }

    /**
     * add producer
     *
     * @param producer
     */
    public Producer addProducer(Producer producer) {
        producerService.add(producer);
        metaMessenger.publish(ProducerEvent.add(producer.getTopic(), producer.getApp()));
        return producer;
    }

    /**
     * remove consumer
     *
     * @param topic
     * @param app
     * @return
     */
    public boolean removeConsumer(TopicName topic, String app) {
        Consumer consumer = new Consumer();
        consumer.setTopic(topic);
        consumer.setApp(app);
        consumerService.delete(consumer);
        metaMessenger.publish(ConsumerEvent.remove(topic, app));
        return true;
    }

    public boolean removeProducer(TopicName topic, String app) {
        Producer producer = new Producer();
        producer.setTopic(topic);
        producer.setApp(app);
        producerService.delete(producer);
        metaMessenger.publish(ProducerEvent.remove(topic, app));
        return true;
    }

    /**
     * get producer
     *
     * @param topic
     * @param app
     * @return
     */
    public Producer getProducer(TopicName topic, String app) {
        return producerService.getByTopicAndApp(topic, app);
    }

    /**
     * get producer
     *
     * @param app
     * @return
     */
    public List<Producer> getProducer(String app) {
        return producerService.getByApp(app, false);
    }

    /**
     * get consumer
     *
     * @param topic
     * @param app
     * @return
     */
    public Consumer getConsumer(TopicName topic, String app) {
        return consumerService.getByTopicAndApp(topic, app);
    }

    /**
     * get consumer
     *
     * @param app
     * @return
     */
    public List<Consumer> getConsumer(String app) {
        return consumerService.getByApp(app, false);
    }

    /**
     * get consumer
     *
     * @param topic
     * @return
     */
    public List<Consumer> getConsumerByTopic(TopicName topic) {
        return consumerService.getByTopic(topic, true);
    }

    /**
     * get producer
     */
    public List<Producer> getProducerByTopic(TopicName topic) {
        return producerService.getByTopic(topic, true);
    }

    /**
     * get partition group
     *
     * @param topic
     * @return
     */
    public List<PartitionGroup> getPartitionGroupByTopic(TopicName topic) {
        return partitionGroupService.getByTopic(topic);
    }


    /**
     * get topic
     *
     * @param brokerId
     * @return
     */
    public Set<TopicName> getTopicByBroker(Integer brokerId) {
        List<Replica> list = partitionGroupReplicaService.findByBrokerId(brokerId);
        Set<TopicName> topics = new HashSet<>();
        if (null != list && list.size() > 0) {
            list.forEach(replica -> topics.add(replica.getTopic()));
        }
        return topics;
    }

    /**
     * get replica
     *
     * @param brokerId
     * @return
     */
    public List<Replica> getReplicaByBroker(Integer brokerId) {
        return partitionGroupReplicaService.findByBrokerId(brokerId);
    }


    /**
     * add topic
     *
     * @param topic
     * @param partitionGroups
     */
    public void addTopic(Topic topic, List<PartitionGroup> partitionGroups) {
        topicService.addTopic(topic, partitionGroups);
    }

    /**
     * find app token
     *
     * @param app
     * @param token
     * @return
     */
    public AppToken findAppToken(String app, String token) {
        return appTokenService.getByAppAndToken(app, token);
    }

    /**
     * list app token
     *
     * @return
     */
    public List<AppToken> listAppToken() {
        return appTokenService.list();
    }

    /**
     * get broker
     *
     * @return
     */
    public List<Broker> getAllBrokers() {
        return brokerService.list();
    }

    /**
     * get data center
     *
     * @return
     */
    public Collection<DataCenter> getAllDataCenter() {
        return dataCenterService.list();
    }

    /**
     * get data center
     *
     * @param code
     * @return
     */
    public DataCenter getDataCenter(String code) {
        return dataCenterService.getById(code);
    }

    /**
     * get config
     *
     * @param group
     * @param key
     * @return
     */
    public Config getConfig(String group, String key) {
        return configService.getByGroupAndKey(group, key);
    }

    /**
     * get config
     *
     * @return
     */
    public List<Config> getAllConfigs() {
        return configService.list();
    }

    /**
     * get topic
     *
     * @return
     */
    public List<Topic> getAllTopics() {
        return topicService.list();
    }

    /**
     * get broker
     *
     * @param retryType
     * @return
     */
    public List<Broker> getBrokerByRetryType(String retryType) {
        return brokerService.getByRetryType(retryType);
    }

    /**
     * update partition group
     *
     * @param partitionGroup
     */
    public void updatePartitionGroup(PartitionGroup partitionGroup) {
        PartitionGroup group = partitionGroupService.get(partitionGroup);
        if (group != null){
            group.setIsrs(partitionGroup.getIsrs());
            group.setLeader(partitionGroup.getLeader());
            group.setTerm(partitionGroup.getTerm());
            partitionGroupService.addOrUpdate(group);
            metaMessenger.publish(PartitionGroupEvent.update(partitionGroup.getTopic(), partitionGroup.getGroup()));
        }

    }

    /**
     * add listener
     *
     * @param listener
     */
    public void addListener(MessageListener listener) {
        metaMessenger.addListener(listener);
    }

    /**
     * get topic service
     *
     * @return
     */
    public TopicService getTopicService() {
        return topicService;
    }

    /**
     * get broker service
     *
     * @return
     */
    public BrokerService getBrokerService() {
        return brokerService;
    }

    /**
     * get consumer service
     *
     * @return
     */
    public ConsumerService getConsumerService() {
        return consumerService;
    }

    /**
     * get producer service
     *
     * @return
     */
    public ProducerService getProducerService() {
        return producerService;
    }

    /**
     * get partition group service
     *
     * @return
     */
    public PartitionGroupService getPartitionGroupService() {
        return partitionGroupService;
    }

    /**
     * get partition group replica service
     *
     * @return
     */
    public PartitionGroupReplicaService getPartitionGroupReplicaService() {
        return partitionGroupReplicaService;
    }

    /**
     * get config service
     *
     * @return
     */
    public ConfigService getConfigService() {
        return configService;
    }
}

