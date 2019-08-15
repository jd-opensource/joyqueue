package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.domain.Subscription;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.HasSubscribe;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class HasSubscribeCodec implements NsrPayloadCodec<HasSubscribe>, Type {

    @Override
    public HasSubscribe decode(Header header, ByteBuf buffer) throws Exception {
        return new HasSubscribe()
                .app(Serializer.readString(buffer))
                .subscribe(Subscription.Type.valueOf(buffer.readByte()));
    }

    @Override
    public void encode(HasSubscribe payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getApp(),buffer);
        buffer.writeByte(payload.getSubscribe().getValue());
    }

    @Override
    public int type() {
        return NsrCommandType.HAS_SUBSCRIBE;
    }
}
