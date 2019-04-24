package com.jd.journalq.broker.protocol.handler;

import com.jd.journalq.broker.protocol.JournalqCommandHandler;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;

/**
 * HeartbeatRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class HeartbeatRequestHandler implements JournalqCommandHandler, Type {

    @Override
    public Command handle(Transport transport, Command command) {
        return BooleanAck.build();
    }

    @Override
    public int type() {
        return JournalqCommandType.HEARTBEAT_REQUEST.getCode();
    }
}