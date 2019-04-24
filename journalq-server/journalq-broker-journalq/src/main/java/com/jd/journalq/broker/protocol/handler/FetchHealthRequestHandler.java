package com.jd.journalq.broker.protocol.handler;

import com.jd.journalq.broker.protocol.JournalqCommandHandler;
import com.jd.journalq.network.command.FetchHealthResponse;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;

/**
 * FetchHealthRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class FetchHealthRequestHandler implements JournalqCommandHandler, Type {

    @Override
    public Command handle(Transport transport, Command command) {
        FetchHealthResponse fetchHealthResponse = new FetchHealthResponse(0);
        return new Command(fetchHealthResponse);
    }

    @Override
    public int type() {
        return JournalqCommandType.FETCH_HEALTH_REQUEST.getCode();
    }
}