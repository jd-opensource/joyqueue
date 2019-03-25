package com.jd.journalq.broker.election.network.codec;

import com.jd.journalq.broker.election.command.ReplicateConsumePosRequest;
import com.jd.journalq.network.transport.codec.PayloadEncoder;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/29
 */
public class ReplicateConsumePosRequestEncoder implements PayloadEncoder<ReplicateConsumePosRequest>, Type {
    @Override
    public void encode(final ReplicateConsumePosRequest request, ByteBuf buffer) throws Exception {
        Serializer.write(request.getConsumePositions(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return CommandType.REPLICATE_CONSUME_POS_REQUEST;
    }
}
