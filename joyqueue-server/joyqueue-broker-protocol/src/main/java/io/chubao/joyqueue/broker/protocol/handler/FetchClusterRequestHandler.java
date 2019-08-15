package io.chubao.joyqueue.broker.protocol.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.broker.config.BrokerConfig;
import io.chubao.joyqueue.broker.helper.SessionHelper;
import io.chubao.joyqueue.broker.protocol.JoyQueueCommandHandler;
import io.chubao.joyqueue.broker.protocol.converter.BrokerNodeConverter;
import io.chubao.joyqueue.broker.protocol.converter.PolicyConverter;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.domain.TopicType;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.command.FetchClusterRequest;
import io.chubao.joyqueue.network.command.FetchClusterResponse;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.command.Topic;
import io.chubao.joyqueue.network.command.TopicPartition;
import io.chubao.joyqueue.network.command.TopicPartitionGroup;
import io.chubao.joyqueue.network.domain.BrokerNode;
import io.chubao.joyqueue.network.session.Connection;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.NameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * FetchClusterRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/30
 */
public class FetchClusterRequestHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FetchClusterRequestHandler.class);

    private BrokerConfig brokerConfig;
    private NameService nameService;
    private BrokerContext brokerContext;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerConfig = brokerContext.getBrokerConfig();
        this.nameService = brokerContext.getNameService();
        this.brokerContext = brokerContext;
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FetchClusterRequest fetchClusterRequest = (FetchClusterRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(fetchClusterRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}, app: {}", transport, fetchClusterRequest.getApp());
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

        // TODO 临时日志
        logger.debug("fetch cluster, address: {}, topics: {}, app: {}, metadata: {}",
                transport, fetchClusterRequest.getTopics(), fetchClusterRequest.getApp(), JSON.toJSONString(fetchClusterResponse));

        return new Command(fetchClusterResponse);
    }

    protected Topic getTopicMetadata(Connection connection, String topic, String app, Map<Integer, BrokerNode> brokers) {
        TopicName topicName = TopicName.parse(topic);
        TopicConfig topicConfig = nameService.getTopicConfig(topicName);

        Topic result = new Topic();
        result.setTopic(topic);
        result.setType(TopicType.TOPIC);

        if (topicConfig == null) {
            logger.warn("topic not exist, topic: {}, app: {}", topic, app);
            result.setCode(JoyQueueCode.FW_TOPIC_NOT_EXIST);
            return result;
        }

        Producer producer = nameService.getProducerByTopicAndApp(topicName, app);
        Consumer consumer = nameService.getConsumerByTopicAndApp(topicName, app);

        if (producer == null && consumer == null && !connection.isSystem()) {
            logger.warn("topic policy not exist, topic: {}, app: {}", topic, app);
            result.setCode(JoyQueueCode.CN_NO_PERMISSION);
            return result;
        }

        if (producer == null) {
            result.setProducerPolicy(PolicyConverter.convertProducer(brokerContext.getProducerPolicy()));
        } else {
            if (producer.getProducerPolicy() == null) {
                result.setProducerPolicy(PolicyConverter.convertProducer(brokerContext.getProducerPolicy()));
            } else {
                result.setProducerPolicy(PolicyConverter.convertProducer(producer.getProducerPolicy()));
            }
        }

        if (consumer == null) {
            result.setConsumerPolicy(PolicyConverter.convertConsumer(brokerContext.getConsumerPolicy()));
            result.setType(TopicType.TOPIC);
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
            DataCenter brokerDataCenter = nameService.getDataCenter(leaderBroker.getIp());
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