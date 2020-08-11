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

import com.google.common.collect.Maps;
import org.joyqueue.broker.protocol.JoyQueueCommandHandler;
import org.joyqueue.broker.protocol.JoyQueueContext;
import org.joyqueue.broker.protocol.JoyQueueContextAware;
import org.joyqueue.broker.protocol.converter.BrokerNodeConverter;
import org.joyqueue.broker.protocol.coordinator.Coordinator;
import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.DataCenter;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.command.FindCoordinatorAckData;
import org.joyqueue.network.command.FindCoordinatorRequest;
import org.joyqueue.network.command.FindCoordinatorResponse;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.domain.BrokerNode;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.NameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * FindCoordinatorRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class FindCoordinatorRequestHandler implements JoyQueueCommandHandler, Type, JoyQueueContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FindCoordinatorRequestHandler.class);

    private Coordinator coordinator;
    private NameService nameService;

    @Override
    public void setJoyQueueContext(JoyQueueContext joyQueueContext) {
        this.coordinator = joyQueueContext.getCoordinator();
        this.nameService = joyQueueContext.getBrokerContext().getNameService();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FindCoordinatorRequest findCoordinatorRequest = (FindCoordinatorRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(findCoordinatorRequest.getApp())) {
            logger.warn("connection does not exist, transport: {}, app: {}", transport, findCoordinatorRequest.getApp());
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Map<String, FindCoordinatorAckData> coordinators = findCoordinators(connection, findCoordinatorRequest.getTopics(), findCoordinatorRequest.getApp());

        FindCoordinatorResponse findCoordinatorResponse = new FindCoordinatorResponse();
        findCoordinatorResponse.setCoordinators(coordinators);
        return new Command(findCoordinatorResponse);
    }

    protected Map<String, FindCoordinatorAckData> findCoordinators(Connection connection, List<String> topics, String app) {
        Broker coordinatorBroker = coordinator.findGroup(app);
        JoyQueueCode code = JoyQueueCode.SUCCESS;
        BrokerNode coordinatorNode = null;

        if (coordinatorBroker != null) {
            DataCenter brokerDataCenter = nameService.getDataCenter(coordinatorBroker.getIp());
            coordinatorNode = BrokerNodeConverter.convertBrokerNode(coordinatorBroker, brokerDataCenter, connection.getRegion());
        } else {
            logger.warn("find coordinator error, coordinator not exist, topics: {}, app: {}, remoteAddress: {}", topics, app, connection.getAddressStr());
            code = JoyQueueCode.FW_COORDINATOR_NOT_AVAILABLE;
        }

        Map<String, FindCoordinatorAckData> result = Maps.newHashMap();
        for (String topic : topics) {
            result.put(topic, new FindCoordinatorAckData(coordinatorNode, code));
        }

        return result;
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FIND_COORDINATOR_REQUEST.getCode();
    }
}
