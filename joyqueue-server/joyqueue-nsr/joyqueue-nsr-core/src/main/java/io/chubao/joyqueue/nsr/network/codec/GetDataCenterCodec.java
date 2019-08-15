package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetDataCenter;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
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
