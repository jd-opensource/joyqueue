package io.chubao.joyqueue.network.codec;

import com.google.common.collect.Maps;
import io.chubao.joyqueue.network.command.AddProducerResponse;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * AddProducerResponse
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public class AddProducerResponseCodec implements PayloadCodec<JoyQueueHeader, AddProducerResponse>, Type {

    @Override
    public AddProducerResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        Map<String, String> result = Maps.newHashMap();
        short producerSize = buffer.readShort();
        for (int i = 0; i < producerSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            String producerId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            result.put(topic, producerId);
        }

        AddProducerResponse addProducerResponse = new AddProducerResponse();
        addProducerResponse.setProducerIds(result);
        return addProducerResponse;
    }

    @Override
    public void encode(AddProducerResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getProducerIds().size());
        for (Map.Entry<String, String> entry : payload.getProducerIds().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            Serializer.write(entry.getValue(), buffer, Serializer.SHORT_SIZE);
        }
    }

    @Override
    public int type() {
        return JoyQueueCommandType.ADD_PRODUCER_RESPONSE.getCode();
    }
}