package com.jd.journalq.broker.election.network.codec;

import com.jd.journalq.broker.election.command.ReplicateConsumePosRequest;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadDecoder;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;


/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/29
 */
public class ReplicateConsumePosRequestDecoder implements PayloadDecoder<JMQHeader>, Type {
    @Override
    public Object decode(final JMQHeader header, final ByteBuf buffer) throws Exception {
        String consumePositions = Serializer.readString(buffer, Serializer.SHORT_SIZE);
        return new ReplicateConsumePosRequest(consumePositions);
    }

    @Override
    public int type() {
        return CommandType.REPLICATE_CONSUME_POS_REQUEST;
    }
}
