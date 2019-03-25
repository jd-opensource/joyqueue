package com.jd.journalq.broker.election.network.codec;

import com.jd.journalq.broker.election.command.ReplicateConsumePosResponse;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadDecoder;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/29
 */
public class ReplicateConsumePosResponseDecoder implements PayloadDecoder<JMQHeader>, Type {

    @Override
    public Object decode(final JMQHeader header, final ByteBuf buffer) {
        return new ReplicateConsumePosResponse(buffer.readBoolean());
    }

    @Override
    public int type() {
        return CommandType.REPLICATE_CONSUME_POS_RESPONSE;
    }
}
