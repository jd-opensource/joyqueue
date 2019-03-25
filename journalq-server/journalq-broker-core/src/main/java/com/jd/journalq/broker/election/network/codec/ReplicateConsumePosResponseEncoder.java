package com.jd.journalq.broker.election.network.codec;

import com.jd.journalq.broker.election.command.ReplicateConsumePosResponse;
import com.jd.journalq.network.transport.codec.PayloadEncoder;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/29
 */
public class ReplicateConsumePosResponseEncoder implements PayloadEncoder<ReplicateConsumePosResponse>, Type {

    @Override
    public void encode(final ReplicateConsumePosResponse response, ByteBuf buffer) {
        buffer.writeBoolean(response.isSuccess());
    }

    @Override
    public int type() {
        return CommandType.REPLICATE_CONSUME_POS_RESPONSE;
    }
}
