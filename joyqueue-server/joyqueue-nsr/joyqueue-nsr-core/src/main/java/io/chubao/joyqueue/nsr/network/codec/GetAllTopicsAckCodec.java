package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetAllTopicsAck;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetAllTopicsAckCodec implements NsrPayloadCodec<GetAllTopicsAck>, Type {
    @Override
    public GetAllTopicsAck decode(Header header, ByteBuf buffer) throws Exception {
        GetAllTopicsAck getAllTopicsAck = new GetAllTopicsAck();
            int topicsSize = buffer.readInt();
            Set<String> topicNames = new HashSet<>(topicsSize);
            for(int i = 0;i<topicsSize;i++){
                topicNames.add(Serializer.readString(buffer));
            }
            getAllTopicsAck.topicNames(topicNames);
        return getAllTopicsAck;
    }

    @Override
    public void encode(GetAllTopicsAck payload, ByteBuf buffer) throws Exception {
        Set<String> topicNames = payload.getTopicNames();
        if(null==topicNames||topicNames.size()<1){
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(topicNames.size());
        for(String topicName : topicNames){
            Serializer.write(topicName,buffer);
        }
    }

    @Override
    public int type() {
        return NsrCommandType.GET_ALL_TOPICS_ACK;
    }
}
