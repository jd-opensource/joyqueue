package com.jd.journalq.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * RemoveConnectionRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/30
 */
public class RemoveConnectionRequest extends JMQPayload {

    @Override
    public int type() {
        return JMQCommandType.REMOVE_CONNECTION.getCode();
    }
}