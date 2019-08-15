package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.HasSubscribeAck;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class HasSubscribeAckCodec implements NsrPayloadCodec<HasSubscribeAck>, Type {

    @Override
    public HasSubscribeAck decode(Header header, ByteBuf buffer) throws Exception {
        return new HasSubscribeAck().have(buffer.readBoolean());
    }

    @Override
    public void encode(HasSubscribeAck payload, ByteBuf buffer) throws Exception {
        buffer.writeBoolean(payload.isHave());
    }

    @Override
    public int type() {
        return NsrCommandType.HAS_SUBSCRIBE_ACK;
    }
}
