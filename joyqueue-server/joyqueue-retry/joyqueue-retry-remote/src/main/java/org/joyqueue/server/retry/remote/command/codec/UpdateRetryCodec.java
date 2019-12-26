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
package org.joyqueue.server.retry.remote.command.codec;

import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.server.retry.remote.command.UpdateRetry;
import io.netty.buffer.ByteBuf;

/**
 * Created by chengzhiliang on 2018/9/17.
 */
public class UpdateRetryCodec implements PayloadCodec<JoyQueueHeader, UpdateRetry>, Type {

    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        if (buffer == null) {
            return null;
        }
        String topic = Serializer.readString(buffer);
        String app = Serializer.readString(buffer);
        byte updateType = buffer.readByte();
        short count = buffer.readShort();
        if (count < 0) {
            count = 0;
        }
        Long[] messages = new Long[count];
        for (int i = 0; i < count; i++) {
            messages[i] = buffer.readLong();
        }
        UpdateRetry updateRetryPayload = new UpdateRetry().topic(topic).app(app).updateType(updateType)
                .updateType(updateType).messages(messages);

        return updateRetryPayload;
    }

    @Override
    public void encode(UpdateRetry payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic(), buffer);
        Serializer.write(payload.getApp(), buffer);
        buffer.writeByte(payload.getUpdateType());
        Long[] messages = payload.getMessages();
        int count = messages == null ? 0 : messages.length;
        buffer.writeShort(count);
        if (count > 0) {
            for (int i = 0; i < messages.length; i++) {
                buffer.writeLong(messages[i]);
            }
        }
    }

    @Override
    public int type() {
        return CommandType.UPDATE_RETRY;
    }
}
