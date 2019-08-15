package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetProducerByTopicAndAppAck;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetProducerByTopicAndAppAckCodec implements NsrPayloadCodec<GetProducerByTopicAndAppAck>, Type {
    @Override
    public GetProducerByTopicAndAppAck decode(Header header, ByteBuf buffer) throws Exception {
        GetProducerByTopicAndAppAck getProducerByTopicAndAppAck = new GetProducerByTopicAndAppAck();
        if(buffer.readBoolean())getProducerByTopicAndAppAck.producer(Serializer.readProducer(header.getVersion(), buffer));
        return getProducerByTopicAndAppAck;
    }

    @Override
    public void encode(GetProducerByTopicAndAppAck payload, ByteBuf buffer) throws Exception {
        if(null==payload.getProducer()){
            buffer.writeBoolean(false);
            return;
        }
        buffer.writeBoolean(true);
        Serializer.write(payload.getHeader().getVersion(), payload.getProducer(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_PRODUCER_BY_TOPIC_AND_APP_ACK;
    }
}
