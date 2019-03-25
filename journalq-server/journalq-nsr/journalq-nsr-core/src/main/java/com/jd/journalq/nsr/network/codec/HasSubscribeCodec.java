package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.domain.Subscription;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.HasSubscribe;
import com.jd.journalq.nsr.network.command.NsrCommandType;
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
