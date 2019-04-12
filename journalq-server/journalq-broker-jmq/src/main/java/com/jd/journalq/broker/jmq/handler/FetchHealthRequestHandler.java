package com.jd.journalq.broker.jmq.handler;

import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.network.command.FetchHealthResponse;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;

/**
 * FetchHealthRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class FetchHealthRequestHandler implements JMQCommandHandler, Type {

    @Override
    public Command handle(Transport transport, Command command) {
        FetchHealthResponse fetchHealthResponse = new FetchHealthResponse(0);
        return new Command(fetchHealthResponse);
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_HEALTH_REQUEST.getCode();
    }
}