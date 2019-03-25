package com.jd.journalq.broker.election.command;

import com.jd.journalq.network.transport.command.JMQPayload;
import com.jd.journalq.network.command.CommandType;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/15
 */
public class ReplicateConsumePosRequest extends JMQPayload {
    private String consumePositions;

    public ReplicateConsumePosRequest(String consumePositions) {
        this.consumePositions = consumePositions;
    }

    public String getConsumePositions() {
        return consumePositions;
    }

    @Override
    public int type() {
        return CommandType.REPLICATE_CONSUME_POS_REQUEST;
    }
}
