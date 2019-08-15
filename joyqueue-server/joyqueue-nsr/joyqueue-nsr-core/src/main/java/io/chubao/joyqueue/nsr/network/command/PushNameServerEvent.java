package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.event.NameServerEvent;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/2/17
 */
public class PushNameServerEvent extends JoyQueuePayload {
    private NameServerEvent event;
    public PushNameServerEvent event(NameServerEvent event){
        this.event = event;
        return this;
    }

    public NameServerEvent getEvent() {
        return event;
    }

    @Override
    public int type() {
        return NsrCommandType.PUSH_NAMESERVER_EVENT;
    }

    @Override
    public String toString() {
        return "PushNameServerEvent{" +
                "event=" + event +
                '}';
    }
}
