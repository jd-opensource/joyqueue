package io.chubao.joyqueue.broker.protocol.handler;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.broker.protocol.JoyQueueCommandHandler;
import io.chubao.joyqueue.broker.helper.SessionHelper;
import io.chubao.joyqueue.broker.monitor.SessionManager;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.command.RemoveConnectionRequest;
import io.chubao.joyqueue.network.session.Connection;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;

/**
 * RemoveConnectionRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/11/30
 */
public class RemoveConnectionRequestHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {

    private SessionManager sessionManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.sessionManager = brokerContext.getSessionManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        RemoveConnectionRequest removeConnectionRequest = (RemoveConnectionRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null) {
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        sessionManager.removeConnection(connection.getId());
        return BooleanAck.build();
    }

    @Override
    public int type() {
        return JoyQueueCommandType.REMOVE_CONNECTION_REQUEST.getCode();
    }
}