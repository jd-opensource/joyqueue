package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.RemoveConnection;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0PayloadCodec;
import org.joyqueue.broker.joyqueue0.util.Serializer;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.session.ConnectionId;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;

/**
 * 移除连接解码器
 */
public class RemoveConnectionCodec implements Joyqueue0PayloadCodec, Type {

    @Override
    public Object decode(Header header, final ByteBuf in) throws Exception {
        RemoveConnection payload = new RemoveConnection();
        // 1字节连接ID长度
        payload.setConnectionId(new ConnectionId(Serializer.readString(in)));

        return payload;
    }

    @Override
    public void encode(Payload payload, ByteBuf buffer) {

    }

    @Override
    public int type() {
        return Joyqueue0CommandType.REMOVE_CONNECTION.getCode();
    }
}