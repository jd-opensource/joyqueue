package com.jd.journalq.broker.helper;

import com.jd.journalq.broker.monitor.SessionManager;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.transport.Transport;

/**
 * session帮助类
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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