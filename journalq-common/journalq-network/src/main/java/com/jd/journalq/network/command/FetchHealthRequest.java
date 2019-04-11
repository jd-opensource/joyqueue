package com.jd.journalq.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * FetchHealthRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class FetchHealthRequest extends JMQPayload {

    @Override
    public int type() {
        return JMQCommandType.HEARTBEAT.getCode();
    }
}