package com.jd.journalq.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * AddConnectionResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/29
 */
public class AddConnectionResponse extends JMQPayload {

    private String connectionId;
    private String notification;

    @Override
    public int type() {
        return JMQCommandType.ADD_CONNECTION_ACK.getCode();
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getNotification() {
        return notification;
    }
}