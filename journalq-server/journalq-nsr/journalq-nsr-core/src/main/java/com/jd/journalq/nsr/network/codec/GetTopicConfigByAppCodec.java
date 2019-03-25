package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.common.domain.Subscription;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Header;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetTopicConfigByApp;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/2/13
 */
public class GetTopicConfigByAppCodec implements NsrPayloadCodec<GetTopicConfigByApp>, Type {
    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        return new GetTopicConfigByApp()
                .app(Serializer.readString(buffer))
                .subscribe(Subscription.Type.valueOf(buffer.readByte()));
    }

    @Override
    public void encode(GetTopicConfigByApp payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getApp(),buffer);
        buffer.writeByte(payload.getSubscribe().getValue());
    }

    @Override
    public int type() {
        return NsrCommandType.GET_TOPICCONFIGS_BY_APP;
    }
}
