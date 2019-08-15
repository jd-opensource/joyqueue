package io.chubao.joyqueue.broker.helper;

import io.chubao.joyqueue.broker.monitor.SessionManager;
import io.chubao.joyqueue.network.session.Connection;
import io.chubao.joyqueue.network.transport.Transport;

/**
 * session帮助类
 *
 * author: gaohaoxiang
 * date: 2018/9/10
 */
public class SessionHelper {

    public static void setConnection(Transport transport, Connection connection) {
        transport.attr().set(SessionManager.CONNECTION_KEY, connection);
    }

    public static Connection getConnection(Transport transport) {
        return transport.attr().get(SessionManager.CONNECTION_KEY);
    }
}