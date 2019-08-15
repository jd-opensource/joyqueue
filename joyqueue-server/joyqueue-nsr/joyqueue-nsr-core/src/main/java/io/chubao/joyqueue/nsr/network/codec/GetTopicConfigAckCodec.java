package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetTopicConfigAck;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
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
            getTopicConfigAck.topicConfig(Serializer.readTopicConfig(buffer, header.getVersion()));
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
