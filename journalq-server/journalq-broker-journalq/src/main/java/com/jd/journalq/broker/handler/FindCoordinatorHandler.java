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
package com.jd.journalq.broker.handler;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.JournalqCommandHandler;
import com.jd.journalq.broker.JournalqContext;
import com.jd.journalq.broker.JournalqContextAware;
import com.jd.journalq.broker.converter.BrokerNodeConverter;
import com.jd.journalq.broker.coordinator.JMQCoordinator;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.DataCenter;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.FindCoordinator;
import com.jd.journalq.network.command.FindCoordinatorAck;
import com.jd.journalq.network.command.FindCoordinatorAckData;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.domain.BrokerNode;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.NameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * FindCoordinatorHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class FindCoordinatorHandler implements JournalqCommandHandler, Type, JournalqContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FindCoordinatorHandler.class);

    private JMQCoordinator coordinator;
    private NameService nameService;

    @Override
    public void setJmqContext(JournalqContext journalqContext) {
        this.coordinator = journalqContext.getCoordinator();
        this.nameService = journalqContext.getBrokerContext().getNameService();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FindCoordinator findCoordinator = (FindCoordinator) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(findCoordinator.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JournalqCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Map<String, FindCoordinatorAckData> coordinators = findCoordinators(connection, findCoordinator.getTopics(), findCoordinator.getApp());

        FindCoordinatorAck findCoordinatorAck = new FindCoordinatorAck();
        findCoordinatorAck.setCoordinators(coordinators);
        return new Command(findCoordinatorAck);
    }

    protected Map<String, FindCoordinatorAckData> findCoordinators(Connection connection, List<String> topics, String app) {
        Broker coordinatorBroker = coordinator.findCoordinator(app);
        JournalqCode code = JournalqCode.SUCCESS;
        BrokerNode coordinatorNode = null;

        if (coordinatorBroker != null) {
            DataCenter brokerDataCenter = nameService.getDataCenter(coordinatorBroker.getIp());
            coordinatorNode = BrokerNodeConverter.convertBrokerNode(coordinatorBroker, brokerDataCenter, connection.getRegion());
        } else {
            logger.warn("find coordinator error, coordinator not exist, topics: {}, app: {}, remoteAddress: {}", topics, app, connection.getAddressStr());
            code = JournalqCode.FW_COORDINATOR_NOT_AVAILABLE;
        }

        Map<String, FindCoordinatorAckData> result = Maps.newHashMap();
        for (String topic : topics) {
            result.put(topic, new FindCoordinatorAckData(coordinatorNode, code));
        }

        return result;
    }

    @Override
    public int type() {
        return JournalqCommandType.FIND_COORDINATOR.getCode();
    }
}