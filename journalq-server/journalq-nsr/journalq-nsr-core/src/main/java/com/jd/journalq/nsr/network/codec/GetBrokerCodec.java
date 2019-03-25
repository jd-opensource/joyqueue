package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetBroker;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetBrokerCodec implements NsrPayloadCodec<GetBroker>, Type {
    @Override
    public GetBroker decode(Header header, ByteBuf buffer) throws Exception {
        return new GetBroker().brokerId(buffer.readInt());
    }

    @Override
    public void encode(GetBroker payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getBrokerId());
    }

    @Override
    public int type() {
        return NsrCommandType.GET_BROKER;
    }
}
