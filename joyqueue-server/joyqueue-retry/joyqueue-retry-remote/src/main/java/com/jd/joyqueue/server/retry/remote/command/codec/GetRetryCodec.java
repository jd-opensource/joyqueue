/**
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
package com.jd.joyqueue.server.retry.remote.command.codec;

import com.jd.joyqueue.network.command.CommandType;
import com.jd.joyqueue.network.serializer.Serializer;
import com.jd.joyqueue.network.transport.codec.JournalqHeader;
import com.jd.joyqueue.network.transport.codec.PayloadCodec;
import com.jd.joyqueue.network.transport.command.Type;
import com.jd.joyqueue.server.retry.remote.command.GetRetry;
import io.netty.buffer.ByteBuf;

/**
 * Created by chengzhiliang on 2018/9/17.
 */
public class GetRetryCodec implements PayloadCodec<JournalqHeader, GetRetry>, Type {
    @Override
    public Object decode(JournalqHeader header, ByteBuf buffer) throws Exception {
        if (buffer == null) {
            return null;
        }
        String topic = Serializer.readString(buffer);
        String app = Serializer.readString(buffer);
        short count = buffer.readShort();
        long startId = buffer.readLong();

        GetRetry getRetryPayload = new GetRetry().topic(topic).app(app).count(count).startId(startId);
        return getRetryPayload;
    }

    @Override
    public void encode(GetRetry payload, ByteBuf buffer) throws Exception {
        // 1字节主题长度
        Serializer.write(payload.getTopic(), buffer);

        // 1字节应用长度
        Serializer.write(payload.getApp(), buffer);

        // 2字节个数
        buffer.writeShort(payload.getCount());
        buffer.writeLong(payload.getStartId());
    }

    @Override
    public int type() {
        return CommandType.GET_RETRY;
    }
}
