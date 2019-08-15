package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetBrokerAck extends JoyQueuePayload {
    private Broker broker;
    public GetBrokerAck broker(Broker broker){
        this.broker = broker;
        return this;
    }

    public Broker getBroker() {
        return broker;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_BROKER_ACK;
    }
}
