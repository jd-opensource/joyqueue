package com.jd.journalq.network.codec;

import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.command.GetTopicsAck;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2018/10/19
 */
public class GetTopicsAckCodec implements PayloadCodec<Header,GetTopicsAck>, Type {
    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        Set<String> topics = new HashSet();
        int topicSize = buffer.readInt();
        for(int i = 0;i<topicSize;i++){
            topics.add(Serializer.readString(buffer));
        }
        return new GetTopicsAck().topics(topics);
    }

    @Override
    public void encode(GetTopicsAck payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getTopics().size());
        for(String topic : payload.getTopics()){
            Serializer.write(topic,buffer);
        }
    }
    @Override
    public int type() {
        return CommandType.GET_TOPICS_ACK;
    }
}
