package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class RegisterAck extends JoyQueuePayload {
    private Broker broker;

    public RegisterAck broker(Broker broker){
        this.broker = broker;
        return this;
    }

    public Broker getBroker() {
        return broker;
    }

    @Override
    public int type() {
        return NsrCommandType.REGISTER_ACK;
    }
}
