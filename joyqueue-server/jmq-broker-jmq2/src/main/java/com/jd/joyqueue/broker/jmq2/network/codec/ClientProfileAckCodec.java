package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.ClientProfileAck;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Header;
import com.jd.joyqueue.broker.jmq2.network.JMQ2PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * 客户端性能应答编码器
 */
public class ClientProfileAckCodec implements JMQ2PayloadCodec<ClientProfileAck>, Type {

    @Override
    public void encode(ClientProfileAck payload, ByteBuf out) throws Exception {
        payload.validate();
        out.writeInt(payload.getInterval());
    }

    @Override
    public Object decode(JMQ2Header header, ByteBuf buffer) throws Exception {
        return null;
    }

    @Override
    public int type() {
        return JMQ2CommandType.CLIENT_PROFILE_ACK.getCode();
    }
}