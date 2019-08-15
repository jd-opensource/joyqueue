package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetConsumerByTopicAndAppAck;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetConsumerByTopicAndAppAckCodec implements NsrPayloadCodec<GetConsumerByTopicAndAppAck>, Type {
    @Override
    public GetConsumerByTopicAndAppAck decode(Header header, ByteBuf buffer) throws Exception {
        GetConsumerByTopicAndAppAck getConsumerByTopicAndAppAck = new GetConsumerByTopicAndAppAck();
        if(buffer.readBoolean()){
            getConsumerByTopicAndAppAck.consumer(Serializer.readConsumer(header.getVersion(), buffer));
        }
        return getConsumerByTopicAndAppAck;
    }

    @Override
    public void encode(GetConsumerByTopicAndAppAck payload, ByteBuf buffer) throws Exception {
        if(null==payload.getConsumer()){
            buffer.writeBoolean(false);
            return;
        }
        buffer.writeBoolean(true);
        Serializer.write(payload.getHeader().getVersion(), payload.getConsumer(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_CONSUMER_BY_TOPIC_AND_APP_ACK;
    }
}
