package io.chubao.joyqueue.broker.protocol.handler;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.broker.protocol.JoyQueueCommandHandler;
import io.chubao.joyqueue.broker.helper.SessionHelper;
import io.chubao.joyqueue.broker.monitor.SessionManager;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.command.RemoveProducerRequest;
import io.chubao.joyqueue.network.session.Connection;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RemoveProducerRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/12/28
 */
public class RemoveProducerRequestHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(RemoveProducerRequestHandler.class);

    private SessionManager sessionManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.sessionManager = brokerContext.getSessionManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        RemoveProducerRequest removeProducerRequest = (RemoveProducerRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(removeProducerRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}, app: {}", transport, removeProducerRequest.getApp());
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        for (String topic : removeProducerRequest.getTopics()) {
            String producerId = connection.getProducer(topic, removeProducerRequest.getApp());
            if (StringUtils.isBlank(producerId)) {
                continue;
            }
            sessionManager.removeProducer(producerId);
        }

        return BooleanAck.build();
    }

    @Override
    public int type() {
        return JoyQueueCommandType.REMOVE_PRODUCER_REQUEST.getCode();
    }
}