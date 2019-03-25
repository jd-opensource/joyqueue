package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetConfig;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetConfigCodec implements NsrPayloadCodec<GetConfig>, Type {
    @Override
    public GetConfig decode(Header header, ByteBuf buffer) throws Exception {
        return new GetConfig().group(Serializer.readString(buffer)).key(Serializer.readString(buffer));
    }

    @Override
    public void encode(GetConfig payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getGroup(),buffer);
        Serializer.write(payload.getKey(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_CONFIG;
    }
}
