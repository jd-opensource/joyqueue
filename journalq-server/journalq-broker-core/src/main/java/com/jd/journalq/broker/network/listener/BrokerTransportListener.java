package com.jd.journalq.broker.network.listener;

import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.monitor.SessionManager;
import com.jd.journalq.network.event.TransportEvent;
import com.jd.journalq.network.event.TransportEventType;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.toolkit.concurrent.EventListener;

/**
 * 通信监听
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public class BrokerTransportListener implements EventListener<TransportEvent> {

    private SessionManager sessionManager;

    public BrokerTransportListener(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void onEvent(TransportEvent event) {
        TransportEventType type = event.getType();
        if (!(type.equals(TransportEventType.CLOSE) || type.equals(TransportEventType.EXCEPTION))) {
            return;
        }
        Connection connection = SessionHelper.getConnection(event.getTransport());
        if (connection == null) {
            return;
        }
        sessionManager.removeConnection(connection.getId());
    }
}