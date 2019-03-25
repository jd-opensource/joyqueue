package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetDataCenter;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetDataCenterCodec implements NsrPayloadCodec<GetDataCenter>, Type {
    @Override
    public GetDataCenter decode(Header header, ByteBuf buffer) throws Exception {
        return new GetDataCenter().ip(Serializer.readString(buffer));
    }

    @Override
    public void encode(GetDataCenter payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getIp(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_DATACENTER;
    }
}
