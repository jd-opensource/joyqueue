package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetConsumerByTopicAndAppAck;
import com.jd.journalq.nsr.network.command.NsrCommandType;
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
            getConsumerByTopicAndAppAck.consumer(Serializer.readConsumer(buffer));
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
        Serializer.write(payload.getConsumer(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_CONSUMER_BY_TOPIC_AND_APP_ACK;
    }
}
