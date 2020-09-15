package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.BooleanAck;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Header;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0PayloadCodec;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.transport.command.Type;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/12
 */
public class BooleanAckCodec implements Joyqueue0PayloadCodec<BooleanAck>, Type {

    @Override
    public Object decode(Joyqueue0Header header, ByteBuf buffer) throws Exception {
        return new BooleanAck();
    }

    @Override
    public void encode(BooleanAck payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public int type() {
        return Joyqueue0CommandType.BOOLEAN_ACK.getCode();
    }
}