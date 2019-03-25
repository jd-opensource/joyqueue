package com.jd.journalq.broker.election.command;

import com.jd.journalq.network.transport.command.JMQPayload;
import com.jd.journalq.network.command.CommandType;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/15
 */
public class ReplicateConsumePosResponse extends JMQPayload {
    private boolean success;

    public ReplicateConsumePosResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public int type() {
        return CommandType.REPLICATE_CONSUME_POS_RESPONSE;
    }
}
