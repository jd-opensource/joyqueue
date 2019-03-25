package com.jd.journalq.common.network.codec;

import com.jd.journalq.common.network.command.Heartbeat;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * HeartbeatCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class HeartbeatCodec implements PayloadCodec<JMQHeader, Heartbeat>, Type {

    @Override
    public Heartbeat decode(JMQHeader header, ByteBuf buffer) throws Exception {
        return new Heartbeat();
    }

    @Override
    public void encode(Heartbeat payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public int type() {
        return JMQCommandType.HEARTBEAT.getCode();
    }
}