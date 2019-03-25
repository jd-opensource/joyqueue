package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.domain.Broker;
import com.jd.journalq.common.network.transport.command.JMQPayload;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetAllBrokersAck extends JMQPayload {
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
