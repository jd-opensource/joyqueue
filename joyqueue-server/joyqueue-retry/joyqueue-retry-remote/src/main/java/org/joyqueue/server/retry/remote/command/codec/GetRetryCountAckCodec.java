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
import org.joyqueue.server.retry.remote.command.GetRetryCountAck;
import io.netty.buffer.ByteBuf;

/**
 * Created by chengzhiliang on 2018/9/17.
 */
public class GetRetryCountAckCodec implements PayloadCodec<JoyQueueHeader, GetRetryCountAck>, Type {
    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        if (buffer == null) {
            return null;
        }

        String topic = Serializer.readString(buffer);
        String app = Serializer.readString(buffer);
        int count = buffer.readInt();

        GetRetryCountAck getRetryCountAckPayload = new GetRetryCountAck().topic(topic).app(app).count(count);
        return getRetryCountAckPayload;
    }

    @Override
    public void encode(GetRetryCountAck payload, ByteBuf buffer) throws Exception {
        // 1字节主题长度
        Serializer.write(payload.getTopic(), buffer);

        // 1字节应用长度
        Serializer.write(payload.getApp(), buffer);

        // 4字节条数
        buffer.writeInt(payload.getCount());
    }

    @Override
    public int type() {
        return CommandType.GET_RETRY_COUNT_ACK;
    }
}
