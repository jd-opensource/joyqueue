package com.jd.journalq.broker.protocol.handler;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.protocol.JournalqCommandHandler;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.monitor.SessionManager;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.command.RemoveConnectionRequest;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;

/**
 * RemoveConnectionRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/30
 */
public class RemoveConnectionRequestHandler implements JournalqCommandHandler, Type, BrokerContextAware {

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
            return BooleanAck.build(JournalqCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        sessionManager.removeConnection(connection.getId());
        return BooleanAck.build();
    }

    @Override
    public int type() {
        return JournalqCommandType.REMOVE_CONNECTION_REQUEST.getCode();
    }
}