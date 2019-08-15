package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.chubao.joyqueue.nsr.network.command.RegisterAck;
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
