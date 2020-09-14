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
package org.joyqueue.broker.protocol.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.cluster.ClusterNameService;
import org.joyqueue.broker.config.BrokerConfig;
import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.broker.protocol.JoyQueueCommandHandler;
import org.joyqueue.broker.protocol.converter.BrokerNodeConverter;
import org.joyqueue.broker.protocol.converter.PolicyConverter;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.DataCenter;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.domain.TopicType;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.command.FetchClusterRequest;
import org.joyqueue.network.command.FetchClusterResponse;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.command.Topic;
import org.joyqueue.network.command.TopicPartition;
import org.joyqueue.network.command.TopicPartitionGroup;
import org.joyqueue.network.domain.BrokerNode;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * FetchClusterRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/11/30
 */
public class FetchClusterRequestHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FetchClusterRequestHandler.class);

    private BrokerConfig brokerConfig;
    private ClusterNameService clusterNameService;
    private BrokerContext brokerContext;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerConfig = brokerContext.getBrokerConfig();
        this.clusterNameService = brokerContext.getClusterNameService();
        this.brokerContext = brokerContext;
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FetchClusterRequest fetchClusterRequest = (FetchClusterRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(fetchClusterRequest.getApp())) {
            logger.warn("connection does not exist, transport: {}, app: {}", transport, fetchClusterRequest.getApp());
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Map<String, Topic> topics = Maps.newHashMapWithExpectedSize(fetchClusterRequest.getTopics().size());
        Map<Integer, BrokerNode> brokers = Maps.newHashMap();

        for (String topicId : fetchClusterRequest.getTopics()) {
            Topic topic = getTopicMetadata(connection, topicId, fetchClusterRequest.getApp(), brokers);
            topics.put(topicId, topic);
        }

        FetchClusterResponse fetchClusterResponse = new FetchClusterResponse();
        fetchClusterResponse.setTopics(topics);
        fetchClusterResponse.setBrokers(brokers);

        if (logger.isDebugEnabled()) {
            logger.debug("fetch cluster, address: {}, topics: {}, app: {}, metadata: {}",
                    transport, fetchClusterRequest.getTopics(), fetchClusterRequest.getApp(), JSON.toJSONString(fetchClusterResponse));
        }

        return new Command(fetchClusterResponse);
    }

    protected Topic getTopicMetadata(Connection connection, String topic, String app, Map<Integer, BrokerNode> brokers) {
        TopicName topicName = TopicName.parse(topic);
        TopicConfig topicConfig = clusterNameService.getTopicConfig(topicName);

        Topic result = new Topic();
        result.setTopic(topic);
        result.setType(TopicType.TOPIC);

        if (topicConfig == null) {
            logger.warn("topic not exist, topic: {}, app: {}", topic, app);
            result.setCode(JoyQueueCode.FW_TOPIC_NOT_EXIST);
            return result;
        }

        Producer producer = clusterNameService.getNameService().getProducerByTopicAndApp(topicName, app);
        Consumer consumer = clusterNameService.getNameService().getConsumerByTopicAndApp(topicName, app);

        if (producer == null && consumer == null && !connection.isSystem()) {
            logger.warn("topic policy not exist, topic: {}, app: {}", topic, app);
            result.setCode(JoyQueueCode.CN_NO_PERMISSION);
            return result;
        }

        if (producer == null) {
            if (connection.isSystem()) {
                result.setProducerPolicy(PolicyConverter.convertProducer(brokerContext.getProducerPolicy()));
            }
        } else {
            if (producer.getProducerPolicy() == null) {
                result.setProducerPolicy(PolicyConverter.convertProducer(brokerContext.getProducerPolicy()));
            } else {
                result.setProducerPolicy(PolicyConverter.convertProducer(producer.getProducerPolicy()));
            }
        }

        if (consumer == null) {
            if (connection.isSystem()) {
                result.setConsumerPolicy(PolicyConverter.convertConsumer(brokerContext.getConsumerPolicy()));
                result.setType(TopicType.TOPIC);
            }
        } else {
            if (consumer.getConsumerPolicy() == null) {
                result.setConsumerPolicy(PolicyConverter.convertConsumer(brokerContext.getConsumerPolicy()));
            } else {
                result.setConsumerPolicy(PolicyConverter.convertConsumer(consumer.getConsumerPolicy()));
            }
            result.setType(consumer.getTopicType());
        }

        result.setCode(JoyQueueCode.SUCCESS);
        result.setPartitionGroups(convertTopicPartitionGroups(connection, topicConfig.getPartitionGroups().values(), brokers));
        return result;
    }

    protected Map<Integer, TopicPartitionGroup> convertTopicPartitionGroups(Connection connection, Collection<PartitionGroup> partitionGroups, Map<Integer, BrokerNode> brokers) {
        Map<Integer, TopicPartitionGroup> result = Maps.newLinkedHashMap();
        for (PartitionGroup partitionGroup : partitionGroups) {
            TopicPartitionGroup topicPartitionGroup = convertTopicPartitionGroup(connection, partitionGroup, brokers);
            if (topicPartitionGroup != null) {
                result.put(partitionGroup.getGroup(), topicPartitionGroup);
            }
        }
        return result;
    }

    protected TopicPartitionGroup convertTopicPartitionGroup(Connection connection, PartitionGroup partitionGroup, Map<Integer, BrokerNode> brokers) {
        Map<Short, TopicPartition> partitions = Maps.newLinkedHashMap();

        Broker leaderBroker = partitionGroup.getLeaderBroker();
        if (leaderBroker != null) {
            DataCenter brokerDataCenter = clusterNameService.getNameService().getDataCenter(leaderBroker.getIp());
            brokers.put(partitionGroup.getLeader(), BrokerNodeConverter.convertBrokerNode(leaderBroker, brokerDataCenter, connection.getRegion()));
        }

        for (Short partition : partitionGroup.getPartitions()) {
            partitions.put(partition, convertTopicPartition(partitionGroup, partition));
        }

        TopicPartitionGroup result = new TopicPartitionGroup();
        result.setId(partitionGroup.getGroup());
        result.setLeader(partitionGroup.getLeader());
        result.setPartitions(partitions);
        return result;
    }

    protected TopicPartition convertTopicPartition(PartitionGroup partitionGroup, short partition) {
        TopicPartition result = new TopicPartition();
        result.setId(partition);
        return result;
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_CLUSTER_REQUEST.getCode();
    }
}
