package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.common.domain.Producer;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Header;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetProducerByTopicAck;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetProducerByTopicAckCodec implements NsrPayloadCodec<GetProducerByTopicAck>, Type {
    @Override
    public GetProducerByTopicAck decode(Header header, ByteBuf buffer) throws Exception {
        GetProducerByTopicAck getProducerByTopicAck = new GetProducerByTopicAck();
            int size = buffer.readInt();
            List<Producer> list = new ArrayList<>(size);
            for(int i = 0;i<size;i++){
                list.add(Serializer.readProducer(buffer));
            }
            getProducerByTopicAck.producers(list);
        return getProducerByTopicAck;
    }

    @Override
    public void encode(GetProducerByTopicAck payload, ByteBuf buffer) throws Exception {
        List<Producer> producers = payload.getProducers();
        if(null==producers){
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(producers.size());
        for(Producer producer : producers){
            Serializer.write(producer,buffer);
        }
    }

    @Override
    public int type() {
        return NsrCommandType.GET_PRODUCER_BY_TOPIC_ACK;
    }
}
