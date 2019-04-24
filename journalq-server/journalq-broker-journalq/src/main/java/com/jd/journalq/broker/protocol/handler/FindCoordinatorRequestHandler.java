package com.jd.journalq.broker.protocol.handler;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.protocol.JournalqCommandHandler;
import com.jd.journalq.broker.protocol.JournalqContext;
import com.jd.journalq.broker.protocol.JournalqContextAware;
import com.jd.journalq.broker.protocol.converter.BrokerNodeConverter;
import com.jd.journalq.broker.protocol.coordinator.Coordinator;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.DataCenter;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.FindCoordinatorAckData;
import com.jd.journalq.network.command.FindCoordinatorRequest;
import com.jd.journalq.network.command.FindCoordinatorResponse;
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
 * FindCoordinatorRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class FindCoordinatorRequestHandler implements JournalqCommandHandler, Type, JournalqContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FindCoordinatorRequestHandler.class);

    private Coordinator coordinator;
    private NameService nameService;

    @Override
    public void setJournalqContext(JournalqContext journalqContext) {
        this.coordinator = journalqContext.getCoordinator();
        this.nameService = journalqContext.getBrokerContext().getNameService();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FindCoordinatorRequest findCoordinatorRequest = (FindCoordinatorRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(findCoordinatorRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JournalqCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Map<String, FindCoordinatorAckData> coordinators = findCoordinators(connection, findCoordinatorRequest.getTopics(), findCoordinatorRequest.getApp());

        FindCoordinatorResponse findCoordinatorResponse = new FindCoordinatorResponse();
        findCoordinatorResponse.setCoordinators(coordinators);
        return new Command(findCoordinatorResponse);
    }

    protected Map<String, FindCoordinatorAckData> findCoordinators(Connection connection, List<String> topics, String app) {
        Broker coordinatorBroker = coordinator.findGroup(app);
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
        return JournalqCommandType.FIND_COORDINATOR_REQUEST.getCode();
    }
}