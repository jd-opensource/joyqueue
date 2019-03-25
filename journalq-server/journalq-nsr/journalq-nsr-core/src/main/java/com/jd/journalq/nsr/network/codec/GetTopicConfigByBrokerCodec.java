package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.common.network.transport.command.Header;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetTopicConfigByBroker;
import com.jd.journalq.nsr.network.command.NsrCommandType;
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
