package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/2/17
 */
public class PushNameServerEventAck extends JoyQueuePayload {
    @Override
    public int type() {
        return NsrCommandType.PUSH_NAMESERVER_EVENT_ACK;
    }
}
