package com.jd.journalq.broker.jmq.handler;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.broker.jmq.JMQContext;
import com.jd.journalq.broker.jmq.JMQContextAware;
import com.jd.journalq.broker.jmq.converter.BrokerNodeConverter;
import com.jd.journalq.broker.jmq.coordinator.JMQCoordinator;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.common.domain.Broker;
import com.jd.journalq.common.domain.DataCenter;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.network.command.BooleanAck;
import com.jd.journalq.common.network.command.FindCoordinator;
import com.jd.journalq.common.network.command.FindCoordinatorAck;
import com.jd.journalq.common.network.command.FindCoordinatorAckData;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.domain.BrokerNode;
import com.jd.journalq.common.network.session.Connection;
import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.Type;
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
public class FindCoordinatorHandler implements JMQCommandHandler, Type, JMQContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FindCoordinatorHandler.class);

    private JMQCoordinator coordinator;
    private NameService nameService;

    @Override
    public void setJmqContext(JMQContext jmqContext) {
        this.coordinator = jmqContext.getCoordinator();
        this.nameService = jmqContext.getBrokerContext().getNameService();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FindCoordinator findCoordinator = (FindCoordinator) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(findCoordinator.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JMQCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Map<String, FindCoordinatorAckData> coordinators = findCoordinators(connection, findCoordinator.getTopics(), findCoordinator.getApp());

        FindCoordinatorAck findCoordinatorAck = new FindCoordinatorAck();
        findCoordinatorAck.setCoordinators(coordinators);
        return new Command(findCoordinatorAck);
    }

    protected Map<String, FindCoordinatorAckData> findCoordinators(Connection connection, List<String> topics, String app) {
        Broker coordinatorBroker = coordinator.findCoordinator(app);
        JMQCode code = JMQCode.SUCCESS;
        BrokerNode coordinatorNode = null;

        if (coordinatorBroker != null) {
            DataCenter brokerDataCenter = nameService.getDataCenter(coordinatorBroker.getIp());
            coordinatorNode = BrokerNodeConverter.convertBrokerNode(coordinatorBroker, brokerDataCenter, connection.getRegion());
        } else {
            logger.warn("find coordinator error, coordinator not exist, topics: {}, app: {}, remoteAddress: {}", topics, app, connection.getAddressStr());
            code = JMQCode.FW_COORDINATOR_NOT_AVAILABLE;
        }

        Map<String, FindCoordinatorAckData> result = Maps.newHashMap();
        for (String topic : topics) {
            result.put(topic, new FindCoordinatorAckData(coordinatorNode, code));
        }

        return result;
    }

    @Override
    public int type() {
        return JMQCommandType.FIND_COORDINATOR.getCode();
    }
}