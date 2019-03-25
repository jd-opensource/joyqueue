package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Header;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetAppToken;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/2/13
 */
public class GetAppTokenCodec implements NsrPayloadCodec<GetAppToken>, Type {
    @Override
    public GetAppToken decode(Header header, ByteBuf buffer) throws Exception {
        return new GetAppToken().app(Serializer.readString(buffer)).token(Serializer.readString(buffer));
    }

    @Override
    public void encode(GetAppToken payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getApp(),buffer);
        Serializer.write(payload.getToken(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_APP_TOKEN;
    }
}
