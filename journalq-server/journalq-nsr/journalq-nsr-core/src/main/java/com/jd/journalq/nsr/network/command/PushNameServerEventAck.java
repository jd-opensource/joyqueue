package com.jd.journalq.nsr.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/2/17
 */
public class PushNameServerEventAck extends JMQPayload {
    @Override
    public int type() {
        return NsrCommandType.PUSH_NAMESERVER_EVENT_ACK;
    }
}
