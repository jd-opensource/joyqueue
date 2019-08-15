package io.chubao.joyqueue.broker.network.listener;

import io.chubao.joyqueue.broker.helper.SessionHelper;
import io.chubao.joyqueue.broker.monitor.SessionManager;
import io.chubao.joyqueue.network.event.TransportEvent;
import io.chubao.joyqueue.network.event.TransportEventType;
import io.chubao.joyqueue.network.session.Connection;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;

/**
 * BrokerTransportListener
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