package io.chubao.joyqueue.broker.protocol.handler;

import com.google.common.collect.Maps;
import io.chubao.joyqueue.broker.protocol.JoyQueueCommandHandler;
import io.chubao.joyqueue.broker.protocol.JoyQueueContext;
import io.chubao.joyqueue.broker.protocol.JoyQueueContextAware;
import io.chubao.joyqueue.broker.protocol.converter.BrokerNodeConverter;
import io.chubao.joyqueue.broker.protocol.coordinator.Coordinator;
import io.chubao.joyqueue.broker.helper.SessionHelper;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.command.FindCoordinatorAckData;
import io.chubao.joyqueue.network.command.FindCoordinatorRequest;
import io.chubao.joyqueue.network.command.FindCoordinatorResponse;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.domain.BrokerNode;
import io.chubao.joyqueue.network.session.Connection;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.NameService;
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
            logger.warn("connection is not exists, transport: {}, app: {}", transport, findCoordinatorRequest.getApp());
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