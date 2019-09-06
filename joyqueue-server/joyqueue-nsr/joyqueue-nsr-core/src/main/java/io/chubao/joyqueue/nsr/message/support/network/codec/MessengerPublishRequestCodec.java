package io.chubao.joyqueue.nsr.message.support.network.codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.message.support.network.command.MessengerPublishRequest;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * MessengerPublishRequestCodec
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class MessengerPublishRequestCodec implements PayloadCodec<JoyQueueHeader, MessengerPublishRequest>, Type {

    @Override
    public MessengerPublishRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        MessengerPublishRequest request = new MessengerPublishRequest();
        request.setType(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        request.setClassType(Serializer.readString(buffer, Serializer.SHORT_SIZE));

        String eventJson = Serializer.readString(buffer, Serializer.INT_SIZE);
        request.setEvent((MetaEvent) parseJson(eventJson, Class.forName(request.getClassType())));
        return request;
    }

    @Override
    public void encode(MessengerPublishRequest payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getType(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getClassType(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(toJson(payload.getEvent()), buffer, Serializer.INT_SIZE);
    }

    protected Object parseJson(String json, Class<?> type) {
        return JSON.parseObject(json, type);
    }

    protected String toJson(Object value) {
        return JSON.toJSONString(value, SerializerFeature.DisableCircularReferenceDetect);
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_MESSENGER_PUBLISH_REQUEST;
    }
}