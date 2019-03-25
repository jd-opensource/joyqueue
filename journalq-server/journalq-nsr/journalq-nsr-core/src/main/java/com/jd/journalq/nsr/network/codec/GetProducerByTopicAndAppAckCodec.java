package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetProducerByTopicAndAppAck;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetProducerByTopicAndAppAckCodec implements NsrPayloadCodec<GetProducerByTopicAndAppAck>, Type {
    @Override
    public GetProducerByTopicAndAppAck decode(Header header, ByteBuf buffer) throws Exception {
        GetProducerByTopicAndAppAck getProducerByTopicAndAppAck = new GetProducerByTopicAndAppAck();
        if(buffer.readBoolean())getProducerByTopicAndAppAck.producer(Serializer.readProducer(buffer));
        return getProducerByTopicAndAppAck;
    }

    @Override
    public void encode(GetProducerByTopicAndAppAck payload, ByteBuf buffer) throws Exception {
        if(null==payload.getProducer()){
            buffer.writeBoolean(false);
            return;
        }
        buffer.writeBoolean(true);
        Serializer.write(payload.getProducer(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_PRODUCER_BY_TOPIC_AND_APP_ACK;
    }
}
