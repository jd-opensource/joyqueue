package com.jd.journalq.broker.election.network.codec;

import com.jd.journalq.broker.election.command.TimeoutNowResponse;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadDecoder;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/10/2
 */
public class TimeoutNowResponseDecoder implements PayloadDecoder<JMQHeader>, Type {
    @Override
    public Object decode(final JMQHeader header, final ByteBuf buffer) throws Exception {
        boolean success = buffer.readBoolean();
        int term = buffer.readInt();
        return new TimeoutNowResponse(success, term);
    }

    @Override
    public int type() {
        return CommandType.RAFT_TIMEOUT_NOW_RESPONSE;
    }
}
