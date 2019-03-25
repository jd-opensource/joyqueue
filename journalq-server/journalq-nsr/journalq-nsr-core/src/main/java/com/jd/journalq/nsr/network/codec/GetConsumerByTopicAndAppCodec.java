package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.common.domain.TopicName;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Header;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetConsumerByTopicAndApp;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetConsumerByTopicAndAppCodec implements NsrPayloadCodec<GetConsumerByTopicAndApp>, Type {
    @Override
    public GetConsumerByTopicAndApp decode(Header header, ByteBuf buffer) throws Exception {
        return new GetConsumerByTopicAndApp().app(Serializer.readString(buffer)).topic(TopicName.parse(Serializer.readString(buffer)));
    }

    @Override
    public void encode(GetConsumerByTopicAndApp payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getApp(),buffer);
        Serializer.write(payload.getTopic().getFullName(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_CONSUMER_BY_TOPIC_AND_APP;
    }
}
