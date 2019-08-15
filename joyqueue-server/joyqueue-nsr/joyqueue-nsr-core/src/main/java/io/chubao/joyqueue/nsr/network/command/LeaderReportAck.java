package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/2/17
 */
public class LeaderReportAck extends JoyQueuePayload {
    @Override
    public int type() {
        return NsrCommandType.LEADER_REPORT_ACK;
    }
}
