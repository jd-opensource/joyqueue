package com.jd.journalq.common.network.codec;

import com.jd.journalq.common.network.command.FetchHealth;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * FetchHealthCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class FetchHealthCodec implements PayloadCodec<JMQHeader, FetchHealth>, Type {

    @Override
    public Object decode(JMQHeader header, ByteBuf buffer) throws Exception {
        return new FetchHealth();
    }

    @Override
    public void encode(FetchHealth payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_HEALTH.getCode();
    }
}