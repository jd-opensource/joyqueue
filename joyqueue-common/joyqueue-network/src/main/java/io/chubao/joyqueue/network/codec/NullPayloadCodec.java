package io.chubao.joyqueue.network.codec;

import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Payload;
import io.chubao.joyqueue.network.transport.command.Types;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2018/10/12
 */
public class NullPayloadCodec implements PayloadCodec, Types {
    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        return null;
    }
    @Override
    public int[] types() {
        return new int[]{CommandType.BOOLEAN_ACK};
    }
    @Override
    public void encode(Payload payload, ByteBuf buffer) throws Exception {

    }
}
