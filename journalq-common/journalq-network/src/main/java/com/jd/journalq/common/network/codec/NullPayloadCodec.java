package com.jd.journalq.common.network.codec;

import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Header;
import com.jd.journalq.common.network.transport.command.Payload;
import com.jd.journalq.common.network.transport.command.Types;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2018/10/12
 */
public class NullPayloadCodec implements PayloadCodec,Types {
    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        return null;
    }
    @Override
    public int[] types() {
        return new int[]{CommandType.BOOLEAN_ACK};
    }
    @Override
    public void encode(Payload payload, ByteBuf buffer) throws Exception {

    }
}
