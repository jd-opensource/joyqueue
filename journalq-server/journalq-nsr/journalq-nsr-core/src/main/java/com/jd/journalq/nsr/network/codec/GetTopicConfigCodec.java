package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.domain.TopicName;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetTopicConfig;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetTopicConfigCodec implements NsrPayloadCodec<GetTopicConfig>, Type {
    @Override
    public GetTopicConfig decode(Header header, ByteBuf buffer) throws Exception {
        return new GetTopicConfig().topic(TopicName.parse(Serializer.readString(buffer)));
    }

    @Override
    public void encode(GetTopicConfig payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic().getFullName(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_TOPICCONFIG;
    }
}
