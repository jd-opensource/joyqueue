package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Header;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetConfigAck;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetConfigAckCodec implements NsrPayloadCodec<GetConfigAck>, Type {
    @Override
    public GetConfigAck decode(Header header, ByteBuf buffer) throws Exception {
        return new GetConfigAck().value(Serializer.readString(buffer));
    }

    @Override
    public void encode(GetConfigAck payload, ByteBuf buffer) throws Exception {
        //todo 是否需要判断有值没有
        Serializer.write(payload.getValue(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_CONFIG_ACK;
    }
}
