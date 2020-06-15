package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.Heartbeat;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Header;
import com.jd.joyqueue.broker.jmq2.network.JMQ2PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * 心跳
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/29
 */
public class HeartbeatCodec implements JMQ2PayloadCodec<Heartbeat>, Type {

    @Override
    public void encode(Heartbeat payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public Object decode(JMQ2Header header, ByteBuf buffer) throws Exception {
        return new Heartbeat();
    }

    @Override
    public int type() {
        return JMQ2CommandType.HEARTBEAT.getCode();
    }
}