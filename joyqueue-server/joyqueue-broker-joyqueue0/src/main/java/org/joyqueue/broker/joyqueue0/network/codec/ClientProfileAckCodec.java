package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.ClientProfileAck;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Header;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0PayloadCodec;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.transport.command.Type;

/**
 * 客户端性能应答编码器
 */
public class ClientProfileAckCodec implements Joyqueue0PayloadCodec<ClientProfileAck>, Type {

    @Override
    public void encode(ClientProfileAck payload, ByteBuf out) throws Exception {
        payload.validate();
        out.writeInt(payload.getInterval());
    }

    @Override
    public Object decode(Joyqueue0Header header, ByteBuf buffer) throws Exception {
        return null;
    }

    @Override
    public int type() {
        return Joyqueue0CommandType.CLIENT_PROFILE_ACK.getCode();
    }
}