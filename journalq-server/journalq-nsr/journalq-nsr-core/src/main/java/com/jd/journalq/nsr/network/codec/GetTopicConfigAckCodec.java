package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Header;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetTopicConfigAck;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetTopicConfigAckCodec implements NsrPayloadCodec<GetTopicConfigAck>, Type {
    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        GetTopicConfigAck getTopicConfigAck = new GetTopicConfigAck();
        if(buffer.readBoolean()){
            getTopicConfigAck.topicConfig(Serializer.readTopicConfig(buffer));
        }
        return getTopicConfigAck;
    }

    @Override
    public void encode(GetTopicConfigAck payload, ByteBuf buffer) throws Exception {
        if(null==payload.getTopicConfig()){
            buffer.writeBoolean(false);
            return;
        }
        buffer.writeBoolean(true);
        Serializer.write(payload.getTopicConfig(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_TOPICCONFIG_ACK;
    }
}
