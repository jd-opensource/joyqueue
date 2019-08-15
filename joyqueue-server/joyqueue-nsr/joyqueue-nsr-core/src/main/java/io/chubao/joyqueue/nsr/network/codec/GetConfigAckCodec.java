package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetConfigAck;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
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
