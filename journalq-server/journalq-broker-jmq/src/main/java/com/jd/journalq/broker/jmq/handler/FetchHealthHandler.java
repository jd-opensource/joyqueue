package com.jd.journalq.broker.jmq.handler;

import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.network.command.FetchHealthAck;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;

/**
 * FetchHealthHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class FetchHealthHandler implements JMQCommandHandler, Type {

    @Override
    public Command handle(Transport transport, Command command) {
        FetchHealthAck fetchHealthAck = new FetchHealthAck(0);
        return new Command(fetchHealthAck);
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_HEALTH.getCode();
    }
}