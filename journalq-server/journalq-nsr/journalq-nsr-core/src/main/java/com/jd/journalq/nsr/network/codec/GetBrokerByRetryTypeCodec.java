package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetBrokerByRetryType;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetBrokerByRetryTypeCodec implements NsrPayloadCodec<GetBrokerByRetryType>, Type {
    @Override
    public GetBrokerByRetryType decode(Header header, ByteBuf buffer) throws Exception {
        return new GetBrokerByRetryType().retryType(Serializer.readString(buffer));
    }

    @Override
    public void encode(GetBrokerByRetryType payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getRetryType(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_BROKER_BY_RETRYTYPE;
    }
}
