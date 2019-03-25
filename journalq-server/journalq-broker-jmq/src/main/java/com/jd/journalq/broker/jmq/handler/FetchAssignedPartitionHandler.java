package com.jd.journalq.broker.jmq.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.broker.jmq.JMQContext;
import com.jd.journalq.broker.jmq.JMQContextAware;
import com.jd.journalq.broker.jmq.coordinator.JMQCoordinator;
import com.jd.journalq.broker.jmq.coordinator.assignment.PartitionAssignmentHandler;
import com.jd.journalq.broker.jmq.coordinator.domain.PartitionAssignment;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.common.domain.DataCenter;
import com.jd.journalq.common.domain.PartitionGroup;
import com.jd.journalq.common.domain.TopicConfig;
import com.jd.journalq.common.domain.TopicName;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.network.command.BooleanAck;
import com.jd.journalq.common.network.command.FetchAssignedPartition;
import com.jd.journalq.common.network.command.FetchAssignedPartitionAck;
import com.jd.journalq.common.network.command.FetchAssignedPartitionAckData;
import com.jd.journalq.common.network.command.FetchAssignedPartitionData;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.session.Connection;
import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.nsr.NameService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * FetchAssignedPartitionHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class FetchAssignedPartitionHandler implements JMQCommandHandler, Type, JMQContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FetchAssignedPartitionHandler.class);

    private JMQCoordinator coordinator;
    private PartitionAssignmentHandler partitionAssignmentHandler;
    private NameService nameService;

    @Override
    public void setJmqContext(JMQContext jmqContext) {
        this.coordinator = jmqContext.getCoordinator();
        this.partitionAssignmentHandler = jmqContext.getPartitionAssignmentHandler();
        this.nameService = jmqContext.getBrokerContext().getNameService();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FetchAssignedPartition fetchAssignedPartition = (FetchAssignedPartition) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        // 客户端是临时连接，用ip作为唯一标识
        String connectionId = ((InetSocketAddress) transport.remoteAddress()).getHostString();

        if (connection == null || !connection.isAuthorized(fetchAssignedPartition.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JMQCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        if (!coordinator.isCurrentCoordinator(fetchAssignedPartition.getApp())) {
            logger.warn("coordinator is not current, app: {}, topics: {}, transport: {}", fetchAssignedPartition.getApp(), fetchAssignedPartition.getData(), transport);
            return BooleanAck.build(JMQCode.FW_COORDINATOR_NOT_AVAILABLE.getCode());
        }

        Map<String, FetchAssignedPartitionAckData> topicPartitions = Maps.newHashMapWithExpectedSize(fetchAssignedPartition.getData().size());
        for (FetchAssignedPartitionData fetchAssignedPartitionData : fetchAssignedPartition.getData()) {
            FetchAssignedPartitionAckData fetchAssignedPartitionAckData = assignPartition(fetchAssignedPartitionData, fetchAssignedPartition.getApp(), connection.getRegion(), connectionId, connection.getAddressStr());
            if (fetchAssignedPartitionAckData == null) {
                logger.warn("partitionAssignment is null, topic: {}, app: {}, transport: {}", fetchAssignedPartitionData, fetchAssignedPartition.getApp(), transport);
                fetchAssignedPartitionAckData = new FetchAssignedPartitionAckData(JMQCode.FW_COORDINATOR_PARTITION_ASSIGNOR_ERROR);
            }
            topicPartitions.put(fetchAssignedPartitionData.getTopic(), fetchAssignedPartitionAckData);
        }

        FetchAssignedPartitionAck fetchAssignedPartitionAck = new FetchAssignedPartitionAck();
        fetchAssignedPartitionAck.setTopicPartitions(topicPartitions);
        return new Command(fetchAssignedPartitionAck);
    }

    protected FetchAssignedPartitionAckData assignPartition(FetchAssignedPartitionData fetchAssignedPartitionData, String app, String region, String connectionId, String connectionHost) {
        TopicName topicName = TopicName.parse(fetchAssignedPartitionData.getTopic());
        TopicConfig topicConfig = nameService.getTopicConfig(topicName);
        if (topicConfig == null) {
            return null;
        }
        List<PartitionGroup> topicPartitionGroups = null;
        if (fetchAssignedPartitionData.isNearby()) {
            topicPartitionGroups = getTopicRegionPartitionGroup(topicConfig, region);
        } else {
            topicPartitionGroups = Lists.newArrayList(topicConfig.getPartitionGroups().values());
        }
        if (CollectionUtils.isEmpty(topicPartitionGroups)) {
            return new FetchAssignedPartitionAckData(JMQCode.FW_COORDINATOR_PARTITION_ASSIGNOR_NO_PARTITIONS);
        }

        PartitionAssignment partitionAssignment = partitionAssignmentHandler.assign(fetchAssignedPartitionData.getTopic(), app, connectionId, connectionHost, fetchAssignedPartitionData.getSessionTimeout(), topicPartitionGroups);
        return new FetchAssignedPartitionAckData(partitionAssignment.getPartitions(), JMQCode.SUCCESS);
    }

    protected List<PartitionGroup> getTopicRegionPartitionGroup(TopicConfig topicConfig, String region) {
        Collection<PartitionGroup> partitionGroups = topicConfig.getPartitionGroups().values();
        List<PartitionGroup> result = Lists.newArrayListWithCapacity(partitionGroups.size());
        for (PartitionGroup partitionGroup : partitionGroups) {
            if (partitionGroup.getLeaderBroker() != null) {
                DataCenter brokerDataCenter = nameService.getDataCenter(partitionGroup.getLeaderBroker().getIp());
                if (StringUtils.isBlank(region) || brokerDataCenter == null || StringUtils.equals(brokerDataCenter.getRegion(), region)) {
                    result.add(partitionGroup);
                }
            }
        }
        return result;
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_ASSIGNED_PARTITION.getCode();
    }
}