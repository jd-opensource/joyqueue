package io.chubao.joyqueue.network.codec;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.command.RemoveProducerRequest;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * RemoveProducerRequestCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public class RemoveProducerRequestCodec implements PayloadCodec<JoyQueueHeader, RemoveProducerRequest>, Type {

    @Override
    public RemoveProducerRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        RemoveProducerRequest removeProducerRequest = new RemoveProducerRequest();

        short topicSize = buffer.readShort();
        List<String> topics = Lists.newArrayListWithCapacity(topicSize);
        for (int i = 0; i < topicSize; i++) {
            topics.add(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        }

        removeProducerRequest.setTopics(topics);
        removeProducerRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return removeProducerRequest;
    }

    @Override
    public void encode(RemoveProducerRequest payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getTopics().size());
        for (String topic : payload.getTopics()) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JoyQueueCommandType.REMOVE_PRODUCER_REQUEST.getCode();
    }
}