package com.jd.journalq.nsr.network.handler;

import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Types;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.nsr.network.NsrCommandHandler;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import com.jd.journalq.nsr.network.command.PushNameServerEvent;
import com.jd.journalq.nsr.network.command.PushNameServerEventAck;

/**
 * @author wylixiaobin
 * Date: 2019/2/17
 */
public class PushNameServerEventHandler implements NsrCommandHandler, Types,com.jd.laf.extension.Type<String> {
    private NameService nameService;

    @Override
    public String type() {
        return THIN_TYPE;
    }

    @Override
    public Command handle(Transport transport, Command command) {
        nameService.addEvent(((PushNameServerEvent)command.getPayload()).getEvent());
        return new Command(new PushNameServerEventAck());
    }

    @Override
    public int[] types() {
        return new int[]{NsrCommandType.PUSH_NAMESERVER_EVENT};
    }

    @Override
    public void setNameService(NameService nameService) {
        this.nameService = nameService;
    }
}
