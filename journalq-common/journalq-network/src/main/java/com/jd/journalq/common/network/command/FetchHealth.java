package com.jd.journalq.common.network.command;

import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * FetchHealth
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class FetchHealth extends JMQPayload {

    @Override
    public int type() {
        return JMQCommandType.HEARTBEAT.getCode();
    }
}