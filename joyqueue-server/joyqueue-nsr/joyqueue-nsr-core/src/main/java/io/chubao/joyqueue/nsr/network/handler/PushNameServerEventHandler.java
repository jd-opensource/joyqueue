package io.chubao.joyqueue.nsr.network.handler;

import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Types;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.nsr.network.NsrCommandHandler;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.chubao.joyqueue.nsr.network.command.PushNameServerEvent;
import io.chubao.joyqueue.nsr.network.command.PushNameServerEventAck;

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
