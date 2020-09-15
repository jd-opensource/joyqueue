package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.Heartbeat;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Header;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0PayloadCodec;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.transport.command.Type;

/**
 * 心跳
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/29
 */
public class HeartbeatCodec implements Joyqueue0PayloadCodec<Heartbeat>, Type {

    @Override
    public void encode(Heartbeat payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public Object decode(Joyqueue0Header header, ByteBuf buffer) throws Exception {
        return new Heartbeat();
    }

    @Override
    public int type() {
        return Joyqueue0CommandType.HEARTBEAT.getCode();
    }
}