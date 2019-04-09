package com.jd.journalq.broker.election.network.codec;

import com.jd.journalq.network.transport.codec.PayloadEncoder;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.broker.election.command.TimeoutNowResponse;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/10/2
 */
public class TimeoutNowResponseEncoder implements PayloadEncoder<TimeoutNowResponse>, Type {
    @Override
    public void encode(final TimeoutNowResponse response, ByteBuf buffer) throws Exception {
        buffer.writeBoolean(response.isSuccess());
        buffer.writeInt(response.getTerm());
    }

    @Override
    public int type() {
        return CommandType.RAFT_TIMEOUT_NOW_RESPONSE;
    }
}
