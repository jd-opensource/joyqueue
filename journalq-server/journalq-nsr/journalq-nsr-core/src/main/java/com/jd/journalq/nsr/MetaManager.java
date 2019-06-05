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
package com.jd.journalq.nsr;

import com.google.common.base.Preconditions;
import com.jd.journalq.domain.AppToken;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.Config;
import com.jd.journalq.domain.Consumer;
import com.jd.journalq.domain.DataCenter;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.Producer;
import com.jd.journalq.domain.Replica;
import com.jd.journalq.domain.Topic;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.event.ConsumerEvent;
import com.jd.journalq.event.MetaEvent;
import com.jd.journalq.event.PartitionGroupEvent;
import com.jd.journalq.event.ProducerEvent;
import com.jd.journalq.nsr.message.MessageListener;
import com.jd.journalq.nsr.message.Messenger;
import com.jd.journalq.nsr.service.AppTokenService;
import com.jd.journalq.nsr.service.BrokerService;
import com.jd.journalq.nsr.service.ConfigService;
import com.jd.journalq.nsr.service.ConsumerService;
import com.jd.journalq.nsr.service.DataCenterService;
import com.jd.journalq.nsr.service.PartitionGroupReplicaService;
import com.jd.journalq.nsr.service.PartitionGroupService;
import com.jd.journalq.nsr.service.ProducerService;
import com.jd.journalq.nsr.service.TopicService;
import com.jd.journalq.toolkit.service.Service;
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

