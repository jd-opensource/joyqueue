package com.jd.journalq.nsr.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetAllConfigs extends JMQPayload {
    @Override
    public int type() {
        return NsrCommandType.GET_ALL_CONFIG;
    }
}
