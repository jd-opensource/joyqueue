package com.jd.journalq.broker.election.command;

import com.jd.journalq.network.transport.command.JMQPayload;
import com.jd.journalq.network.command.CommandType;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/15
 */
public class TimeoutNowResponse extends JMQPayload {
    private boolean success;
    private int term;

    public TimeoutNowResponse(boolean success, int term) {
        this.success = success;
        this.term = term;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    @Override
    public int type() {
        return CommandType.RAFT_TIMEOUT_NOW_RESPONSE;
    }
}
