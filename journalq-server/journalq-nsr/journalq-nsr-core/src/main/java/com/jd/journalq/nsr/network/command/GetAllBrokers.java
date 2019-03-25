package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetAllBrokers extends JMQPayload {
    @Override
    public int type() {
        return NsrCommandType.GET_ALL_BROKERS;
    }
}
