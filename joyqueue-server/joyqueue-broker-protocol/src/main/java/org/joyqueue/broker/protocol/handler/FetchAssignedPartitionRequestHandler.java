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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.broker.protocol.JoyQueueCommandHandler;
import org.joyqueue.broker.protocol.JoyQueueContext;
import org.joyqueue.broker.protocol.JoyQueueContextAware;
import org.joyqueue.broker.protocol.coordinator.Coordinator;
import org.joyqueue.broker.protocol.coordinator.assignment.PartitionAssignmentHandler;
import org.joyqueue.broker.protocol.coordinator.domain.PartitionAssignment;
import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.domain.DataCenter;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.command.FetchAssignedPartitionAckData;
import org.joyqueue.network.command.FetchAssignedPartitionData;
import org.joyqueue.network.command.FetchAssignedPartitionRequest;
import org.joyqueue.network.command.FetchAssignedPartitionResponse;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.NameService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * FetchAssignedPartitionRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
// TODO 部分逻辑移到partitionAssignmentHandler
public class FetchAssignedPartitionRequestHandler implements JoyQueueCommandHandler, Type, JoyQueueContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FetchAssignedPartitionRequestHandler.class);

    private Coordinator coordinator;
    private PartitionAssignmentHandler partitionAssignmentHandler;
    private NameService nameService;

    @Override
    public void setJoyQueueContext(JoyQueueContext joyQueueContext) {
        this.coordinator = joyQueueContext.getCoordinator();
        this.partitionAssignmentHandler = joyQueueContext.getPartitionAssignmentHandler();
        this.nameService = joyQueueContext.getBrokerContext().getNameService();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FetchAssignedPartitionRequest fetchAssignedPartitionRequest = (FetchAssignedPartitionRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        // 客户端是临时连接，用ip作为唯一标识
        String connectionId = ((InetSocketAddress) transport.remoteAddress()).getHostString();

        if (connection == null || !connection.isAuthorized(fetchAssignedPartitionRequest.getApp())) {
            logger.warn("connection does not exist, transport: {}, app: {}", transport, fetchAssignedPartitionRequest.getApp());
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        if (!coordinator.isCurrentGroup(fetchAssignedPartitionRequest.getApp())) {
            logger.warn("coordinator is not current, app: {}, topics: {}, transport: {}", fetchAssignedPartitionRequest.getApp(), fetchAssignedPartitionRequest.getData(), transport);
            return BooleanAck.build(JoyQueueCode.FW_COORDINATOR_NOT_AVAILABLE.getCode());
        }

        Map<String, FetchAssignedPartitionAckData> topicPartitions = Maps.newHashMapWithExpectedSize(fetchAssignedPartitionRequest.getData().size());
        for (FetchAssignedPartitionData fetchAssignedPartitionData : fetchAssignedPartitionRequest.getData()) {
            FetchAssignedPartitionAckData fetchAssignedPartitionAckData = assignPartition(fetchAssignedPartitionData,
                    fetchAssignedPartitionRequest.getApp(), connection.getRegion(), connectionId, connection.getAddressStr());

            if (fetchAssignedPartitionAckData == null) {
                logger.warn("partitionAssignment is null, topic: {}, app: {}, transport: {}", fetchAssignedPartitionData, fetchAssignedPartitionRequest.getApp(), transport);
                fetchAssignedPartitionAckData = new FetchAssignedPartitionAckData(JoyQueueCode.FW_COORDINATOR_PARTITION_ASSIGNOR_ERROR);
            }
            topicPartitions.put(fetchAssignedPartitionData.getTopic(), fetchAssignedPartitionAckData);
        }

        FetchAssignedPartitionResponse fetchAssignedPartitionResponse = new FetchAssignedPartitionResponse();
        fetchAssignedPartitionResponse.setTopicPartitions(topicPartitions);
        return new Command(fetchAssignedPartitionResponse);
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
            return new FetchAssignedPartitionAckData(JoyQueueCode.FW_COORDINATOR_PARTITION_ASSIGNOR_NO_PARTITIONS);
        }

        PartitionAssignment partitionAssignment = partitionAssignmentHandler.assign(fetchAssignedPartitionData.getTopic(),
                app, connectionId, connectionHost, fetchAssignedPartitionData.getSessionTimeout(), topicPartitionGroups);
        return new FetchAssignedPartitionAckData(partitionAssignment.getPartitions(), JoyQueueCode.SUCCESS);
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
        return JoyQueueCommandType.FETCH_ASSIGNED_PARTITION_REQUEST.getCode();
    }
}
