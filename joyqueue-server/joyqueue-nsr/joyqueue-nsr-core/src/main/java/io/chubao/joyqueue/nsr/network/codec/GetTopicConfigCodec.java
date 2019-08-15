package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetTopicConfig;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
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
