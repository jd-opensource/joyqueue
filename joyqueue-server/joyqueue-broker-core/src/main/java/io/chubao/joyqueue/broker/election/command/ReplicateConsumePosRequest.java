package io.chubao.joyqueue.broker.election.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;
import io.chubao.joyqueue.network.command.CommandType;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/15
 */
public class ReplicateConsumePosRequest extends JoyQueuePayload {
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
