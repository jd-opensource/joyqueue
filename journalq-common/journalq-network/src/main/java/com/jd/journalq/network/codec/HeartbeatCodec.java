package com.jd.journalq.network.codec;

import com.jd.journalq.network.command.HeartbeatRequest;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * HeartbeatCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class HeartbeatCodec implements PayloadCodec<JMQHeader, HeartbeatRequest>, Type {

    @Override
    public HeartbeatRequest decode(JMQHeader header, ByteBuf buffer) throws Exception {
        return new HeartbeatRequest();
    }

    @Override
    public void encode(HeartbeatRequest payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public int type() {
        return JMQCommandType.HEARTBEAT.getCode();
    }
}