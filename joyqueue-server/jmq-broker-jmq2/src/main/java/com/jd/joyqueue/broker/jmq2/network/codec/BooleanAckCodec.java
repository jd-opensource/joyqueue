package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.BooleanAck;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Header;
import com.jd.joyqueue.broker.jmq2.network.JMQ2PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/12
 */
public class BooleanAckCodec implements JMQ2PayloadCodec<BooleanAck>, Type {

    @Override
    public Object decode(JMQ2Header header, ByteBuf buffer) throws Exception {
        return new BooleanAck();
    }

    @Override
    public void encode(BooleanAck payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public int type() {
        return JMQ2CommandType.BOOLEAN_ACK.getCode();
    }
}