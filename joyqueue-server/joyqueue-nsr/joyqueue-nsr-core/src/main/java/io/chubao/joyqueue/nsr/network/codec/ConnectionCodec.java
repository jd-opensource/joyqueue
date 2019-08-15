package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.chubao.joyqueue.nsr.network.command.NsrConnection;
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
