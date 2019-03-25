package com.jd.journalq.common.network.transport.command;

import com.jd.journalq.common.network.transport.codec.JMQHeader;

/**
 * JMQCommand
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/29
 */
public class JMQCommand extends Command {

    public JMQCommand(JMQPayload payload) {
        this(payload.type(), payload);
    }

    public JMQCommand(int type, JMQPayload payload) {
        setHeader(new JMQHeader(type));
        setPayload(payload);
    }

    @Override
    public String toString() {
        return header.toString();
    }
}