package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.common.network.transport.command.Header;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import com.jd.journalq.nsr.network.command.NsrConnection;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/3/15
 */
public class ConnectionCodec implements NsrPayloadCodec<NsrConnection>, Type {
    @Override
    public NsrConnection decode(Header header, ByteBuf buffer) throws Exception {
        return new NsrConnection().brokerId(buffer.readInt());
    }

    @Override
    public void encode(NsrConnection nsrConnection, ByteBuf buffer) throws Exception {
        buffer.writeInt(nsrConnection.getBrokerId());
    }

    @Override
    public int type() {
        return NsrCommandType.CONNECT;
    }
}
