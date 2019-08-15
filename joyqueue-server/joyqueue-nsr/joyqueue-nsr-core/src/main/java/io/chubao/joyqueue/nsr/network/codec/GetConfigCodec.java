package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetConfig;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
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
