package io.chubao.joyqueue.network.codec;

import com.google.common.collect.Maps;
import io.chubao.joyqueue.network.command.AddConsumerResponse;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * AddConsumerResponseCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public class AddConsumerResponseCodec implements PayloadCodec<JoyQueueHeader, AddConsumerResponse>, Type {

    @Override
    public AddConsumerResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        Map<String, String> result = Maps.newHashMap();
        short consumerSize = buffer.readShort();
        for (int i = 0; i < consumerSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            String consumerId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            result.put(topic, consumerId);
        }

        AddConsumerResponse addConsumerResponse = new AddConsumerResponse();
        addConsumerResponse.setConsumerIds(result);
        return addConsumerResponse;
    }

    @Override
    public void encode(AddConsumerResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getConsumerIds().size());
        for (Map.Entry<String, String> entry : payload.getConsumerIds().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            Serializer.write(entry.getValue(), buffer, Serializer.SHORT_SIZE);
        }
    }

    @Override
    public int type() {
        return JoyQueueCommandType.ADD_CONSUMER_RESPONSE.getCode();
    }
}