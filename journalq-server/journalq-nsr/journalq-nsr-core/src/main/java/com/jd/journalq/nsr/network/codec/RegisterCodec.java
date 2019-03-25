package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Header;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import com.jd.journalq.nsr.network.command.Register;
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
