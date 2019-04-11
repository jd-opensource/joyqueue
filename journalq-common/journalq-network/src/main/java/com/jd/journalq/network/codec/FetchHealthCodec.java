package com.jd.journalq.network.codec;

import com.jd.journalq.network.command.FetchHealthRequest;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * FetchHealthCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class FetchHealthCodec implements PayloadCodec<JMQHeader, FetchHealthRequest>, Type {

    @Override
    public Object decode(JMQHeader header, ByteBuf buffer) throws Exception {
        return new FetchHealthRequest();
    }

    @Override
    public void encode(FetchHealthRequest payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_HEALTH.getCode();
    }
}