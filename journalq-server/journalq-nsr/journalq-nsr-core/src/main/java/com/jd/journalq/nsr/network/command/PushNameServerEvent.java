package com.jd.journalq.nsr.network.command;

import com.jd.journalq.event.NameServerEvent;
import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/2/17
 */
public class PushNameServerEvent extends JMQPayload {
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
