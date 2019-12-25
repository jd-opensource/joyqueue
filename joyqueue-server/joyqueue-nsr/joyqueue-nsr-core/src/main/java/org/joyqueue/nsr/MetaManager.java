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
package org.joyqueue.nsr;

import com.google.common.base.Preconditions;
import org.joyqueue.domain.AppToken;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.Config;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.DataCenter;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.Replica;
import org.joyqueue.domain.Topic;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.service.AppTokenService;
import org.joyqueue.nsr.service.BrokerService;
import org.joyqueue.nsr.service.ConfigService;
import org.joyqueue.nsr.service.ConsumerService;
import org.joyqueue.nsr.service.DataCenterService;
import org.joyqueue.nsr.service.PartitionGroupReplicaService;
import org.joyqueue.nsr.service.PartitionGroupService;
import org.joyqueue.nsr.service.ProducerService;
import org.joyqueue.nsr.service.TopicService;
import org.joyqueue.toolkit.service.Service;
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

    /**
     * construct
     *
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
    public MetaManager(ConfigService configService,
                       TopicService topicService,
                       BrokerService brokerService,
                       ConsumerService consumerService,
                       ProducerService producerService,
                       PartitionGroupService partitionGroupService,
                       PartitionGroupReplicaService partitionGroupReplicaService,
                       AppTokenService appTokenService,
                       DataCenterService dataCenterService) {

        Preconditions.checkArgument(topicService != null, "topic service can not be null");
        Preconditions.checkArgument(brokerService != null, "broker service can not be null");
        Preconditions.checkArgument(consumerService != null, "consumer service can not be null");
        Preconditions.checkArgument(producerService != null, "producer service can not be null");
        Preconditions.checkArgument(dataCenterService != null, "data center service can not be null");
        Preconditions.checkArgument(partitionGroupService != null, "partition group service can not be null");
        Preconditions.checkArgument(partitionGroupReplicaService != null, "partition group replica service can not be null");
        this.topicService = topicService;
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
        brokerService.add(broker);
        return true;
    }

    public boolean updateBroker(Broker broker) {
        brokerService.update(broker);
        return true;
    }

    /**
     * add consumer
     *
     * @param consumer
     * @return
     */
    public Consumer addConsumer(Consumer consumer) {
        consumerService.add(consumer);
        return consumer;
    }

    /**
     * add producer
     *
     * @param producer
     */
    public Producer addProducer(Producer producer) {
        producerService.add(producer);
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
        consumerService.delete(consumer.getId());
        return true;
    }

    public boolean removeProducer(TopicName topic, String app) {
        Producer producer = new Producer();
        producer.setTopic(topic);
        producer.setApp(app);
        producerService.delete(producer.getId());
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
        return producerService.getByApp(app);
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
        return consumerService.getByApp(app);
    }

    /**
     * get consumer
     *
     * @param topic
     * @return
     */
    public List<Consumer> getConsumerByTopic(TopicName topic) {
        return consumerService.getByTopic(topic);
    }

    /**
     * get producer
     */
    public List<Producer> getProducerByTopic(TopicName topic) {
        return producerService.getByTopic(topic);
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
        List<Replica> list = partitionGroupReplicaService.getByBrokerId(brokerId);
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
        return partitionGroupReplicaService.getByBrokerId(brokerId);
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
     * get broker
     *
     * @return
     */
    public List<Broker> getAllBrokers() {
        return brokerService.getAll();
    }

    /**
     * get data center
     *
     * @return
     */
    public Collection<DataCenter> getAllDataCenter() {
        return dataCenterService.getAll();
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
        return configService.getAll();
    }

    /**
     * get topic
     *
     * @return
     */
    public List<Topic> getAllTopics() {
        return topicService.getAll();
    }

    public List<PartitionGroup> getAllPartitionGroups() {
        return partitionGroupService.getAll();
    }

    public List<Consumer> getAllConsumers() {
        return consumerService.getAll();
    }

    public List<Producer> getAllProducers() {
        return producerService.getAll();
    }

    public List<AppToken> getAllAppToken() {
        return appTokenService.getAll();
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
    public void leaderReport(PartitionGroup partitionGroup) {
        topicService.leaderReport(partitionGroup);
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

    public AppTokenService getAppTokenService() {
        return appTokenService;
    }
}

