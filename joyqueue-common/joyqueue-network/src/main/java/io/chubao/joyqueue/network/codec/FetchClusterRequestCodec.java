package io.chubao.joyqueue.network.codec;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.network.command.FetchClusterRequest;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * FetchClusterRequestCodec
 *
 * author: gaohaoxiang
 * date: 2018/11/30
 */
public class FetchClusterRequestCodec implements PayloadCodec<JoyQueueHeader, FetchClusterRequest>, Type {

    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        FetchClusterRequest fetchClusterRequest = new FetchClusterRequest();

        short topicSize = buffer.readShort();
        List<String> topics = Lists.newArrayListWithCapacity(topicSize);
        for (int i = 0; i < topicSize; i++) {
            topics.add(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        }

        fetchClusterRequest.setTopics(topics);
        fetchClusterRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return fetchClusterRequest;
    }

    @Override
    public void encode(FetchClusterRequest payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getTopics().size());
        for (String topic : payload.getTopics()) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_CLUSTER_REQUEST.getCode();
    }
}