package io.chubao.joyqueue.network.codec;

import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.network.command.GetTopics;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2018/10/19
 */
public class GetTopicsCodec implements PayloadCodec<Header,GetTopics>, Type {
    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        return new GetTopics().app(Serializer.readString(buffer)).subscribeType(buffer.readInt());
    }

    @Override
    public void encode(GetTopics payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getApp(),buffer);
        buffer.writeInt(payload.getSubscribeType());
    }

    @Override
    public int type() {
        return CommandType.GET_TOPICS;
    }
}
