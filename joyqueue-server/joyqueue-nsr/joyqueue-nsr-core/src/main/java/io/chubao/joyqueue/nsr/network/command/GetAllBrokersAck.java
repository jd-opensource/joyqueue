package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetAllBrokersAck extends JoyQueuePayload {
    private List<Broker> brokers;
    public GetAllBrokersAck brokers(List<Broker> brokers){
        this.brokers = brokers;
        return this;
    }

    public List<Broker> getBrokers() {
        return brokers;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_ALL_BROKERS_ACK;
    }
}
