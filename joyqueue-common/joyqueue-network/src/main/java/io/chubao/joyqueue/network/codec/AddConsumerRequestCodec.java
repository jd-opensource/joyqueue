package io.chubao.joyqueue.network.codec;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.network.command.AddConsumerRequest;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;


/**
 * AddConsumerRequestCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public class AddConsumerRequestCodec implements PayloadCodec<JoyQueueHeader, AddConsumerRequest>, Type {

    @Override
    public AddConsumerRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        AddConsumerRequest addConsumerRequest = new AddConsumerRequest();

        short topicSize = buffer.readShort();
        List<String> topics = Lists.newArrayListWithCapacity(topicSize);
        for (int i = 0; i < topicSize; i++) {
            topics.add(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        }

        addConsumerRequest.setTopics(topics);
        addConsumerRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        addConsumerRequest.setSequence(buffer.readLong());
        return addConsumerRequest;
    }

    @Override
    public void encode(AddConsumerRequest payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getTopics().size());
        for (String topic : payload.getTopics()) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
        buffer.writeLong(payload.getSequence());
    }

    @Override
    public int type() {
        return JoyQueueCommandType.ADD_CONSUMER_REQUEST.getCode();
    }
}