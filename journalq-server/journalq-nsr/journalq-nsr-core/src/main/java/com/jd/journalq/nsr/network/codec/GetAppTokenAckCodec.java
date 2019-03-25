package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Header;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetAppTokenAck;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/2/13
 */
public class GetAppTokenAckCodec implements NsrPayloadCodec<GetAppTokenAck>, Type {
    @Override
    public GetAppTokenAck decode(Header header, ByteBuf buffer) throws Exception {
        GetAppTokenAck appTokenAck = new GetAppTokenAck();
        if(buffer.readBoolean()){
            appTokenAck.appToken(Serializer.readAppToken(buffer));
        }
        return appTokenAck;
    }

    @Override
    public void encode(GetAppTokenAck payload, ByteBuf buffer) throws Exception {
        if(null==payload.getAppToken()){
            buffer.writeBoolean(false);
            return;
        }
        buffer.writeBoolean(true);
        Serializer.write(payload.getAppToken(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_APP_TOKEN_ACK;
    }
}
