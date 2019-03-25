package com.jd.journalq.broker.election.network.codec;

import com.jd.journalq.network.transport.codec.PayloadEncoder;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.broker.election.command.TimeoutNowRequest;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/10/2
 */
public class TimeoutNowRequestEncoder implements PayloadEncoder<TimeoutNowRequest>, Type {
    @Override
    public void encode(final TimeoutNowRequest request, ByteBuf buffer) throws Exception {
        Serializer.write(request.getTopic(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(request.getPartitionGroup());
        buffer.writeInt(request.getTerm());
    }

    @Override
    public int type() {
        return CommandType.RAFT_TIMEOUT_NOW_REQUEST;
    }
}
