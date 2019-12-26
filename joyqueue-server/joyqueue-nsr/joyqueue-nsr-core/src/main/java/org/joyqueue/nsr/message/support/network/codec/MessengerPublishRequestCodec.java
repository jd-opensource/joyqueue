/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.nsr.message.support.network.codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.message.support.network.command.MessengerPublishRequest;
import org.joyqueue.nsr.network.command.NsrCommandType;
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