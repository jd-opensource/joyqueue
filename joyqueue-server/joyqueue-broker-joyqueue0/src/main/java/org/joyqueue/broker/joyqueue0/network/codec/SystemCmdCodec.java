package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.SystemCmd;
import org.joyqueue.broker.joyqueue0.util.Serializer;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.transport.codec.PayloadDecoder;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;

/**
 * 系统命令解码器
 */
public class SystemCmdCodec implements PayloadDecoder, Type {

    @Override
    public Object decode(Header header, ByteBuf in) throws Exception {
        SystemCmd payload = new SystemCmd();
        payload.setCmd(Serializer.readString(in, 1));
        payload.setUrl(Serializer.readString(in, 2));
        payload.setTimeout(in.readInt());
        return payload;
    }

    @Override
    public int type() {
        return Joyqueue0CommandType.SYSTEM_COMMAND.getCode();
    }
}