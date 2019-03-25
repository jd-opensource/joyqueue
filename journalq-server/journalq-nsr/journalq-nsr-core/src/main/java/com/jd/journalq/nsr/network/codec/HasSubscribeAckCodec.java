package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.common.network.transport.command.Header;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.HasSubscribeAck;
import com.jd.journalq.nsr.network.command.NsrCommandType;
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
