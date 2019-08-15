package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.chubao.joyqueue.nsr.network.command.Register;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class RegisterCodec implements NsrPayloadCodec<Register>, Type {
    @Override
    public Register decode(Header header, ByteBuf buffer) throws Exception {
        int brokerId = buffer.readInt();
        return new Register().brokerId(brokerId>0?brokerId:null).brokerIp(Serializer.readString(buffer)).port(buffer.readInt());
    }

    @Override
    public void encode(Register payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(null==payload.getBrokerId()?0:payload.getBrokerId());
        Serializer.write(payload.getBrokerIp(),buffer);
        buffer.writeInt(payload.getPort());
    }

    @Override
    public int type() {
        return NsrCommandType.REGISTER;
    }
}
