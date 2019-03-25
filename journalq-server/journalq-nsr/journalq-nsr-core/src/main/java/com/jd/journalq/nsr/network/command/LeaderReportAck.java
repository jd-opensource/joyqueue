package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/2/17
 */
public class LeaderReportAck extends JMQPayload {
    @Override
    public int type() {
        return NsrCommandType.LEADER_REPORT_ACK;
    }
}
