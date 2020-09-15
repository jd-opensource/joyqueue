package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.GetOffset;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.transport.codec.PayloadDecoder;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;

/**
 * 获取偏移量解码器
 */
public class GetOffsetCodec implements PayloadDecoder, Type {

    @Override
    public Object decode(Header header, final ByteBuf in) throws Exception {
        GetOffset payload = new GetOffset();
        payload.setOffset(in.readLong());
        payload.setOptimized(in.readByte() == 1);
        return payload;
    }

    @Override
    public int type() {
        return Joyqueue0CommandType.GET_OFFSET.getCode();
    }
}