package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetTopicConfigByBroker;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetTopicConfigByBrokerCodec implements NsrPayloadCodec<GetTopicConfigByBroker>, Type {
    @Override
    public GetTopicConfigByBroker decode(Header header, ByteBuf buffer) throws Exception {
        return new GetTopicConfigByBroker().brokerId(buffer.readInt());
    }

    @Override
    public void encode(GetTopicConfigByBroker payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getBrokerId());
    }

    @Override
    public int type() {
        return NsrCommandType.GET_TOPICCONFIGS_BY_BROKER;
    }
}
