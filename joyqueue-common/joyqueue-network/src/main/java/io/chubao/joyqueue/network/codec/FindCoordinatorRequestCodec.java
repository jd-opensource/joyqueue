package io.chubao.joyqueue.network.codec;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.network.command.FindCoordinatorRequest;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * FindCoordinatorRequestCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class FindCoordinatorRequestCodec implements PayloadCodec<JoyQueueHeader, FindCoordinatorRequest>, Type {

    @Override
    public FindCoordinatorRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        FindCoordinatorRequest findCoordinatorRequest = new FindCoordinatorRequest();

        short topicSize = buffer.readShort();
        List<String> topics = Lists.newArrayListWithCapacity(topicSize);
        for (int i = 0; i < topicSize; i++) {
            topics.add(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        }

        findCoordinatorRequest.setTopics(topics);
        findCoordinatorRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return findCoordinatorRequest;
    }

    @Override
    public void encode(FindCoordinatorRequest payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getTopics().size());
        for (String topic : payload.getTopics()) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FIND_COORDINATOR_REQUEST.getCode();
    }
}