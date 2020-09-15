package org.joyqueue.broker.joyqueue0.command;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;

/**
 * 合并消费位置应答
 */
public class GetConsumeOffsetAck extends GetConsumeOffset {
    @Override
    public int type() {
        return Joyqueue0CommandType.GET_CONSUMER_OFFSET_ACK.getCode();
    }
}
