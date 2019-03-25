package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Header;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import com.jd.journalq.nsr.network.command.RegisterAck;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class RegisterAckCodec implements NsrPayloadCodec<RegisterAck>, Type {
    @Override
    public RegisterAck decode(Header header, ByteBuf buffer) throws Exception {
        return new RegisterAck().broker(Serializer.readBroker(buffer));
    }

    @Override
    public void encode(RegisterAck payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getBroker(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.REGISTER_ACK;
    }
}
