package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetTopicConfigByBroker extends JMQPayload {
    private int brokerId;
    public GetTopicConfigByBroker brokerId(int brokerId){
        this.brokerId = brokerId;
        return this;
    }

    public int getBrokerId() {
        return brokerId;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_TOPICCONFIGS_BY_BROKER;
    }

    @Override
    public String toString() {
        return "GetTopicConfigByBroker{" +
                "brokerId=" + brokerId +
                '}';
    }
}
