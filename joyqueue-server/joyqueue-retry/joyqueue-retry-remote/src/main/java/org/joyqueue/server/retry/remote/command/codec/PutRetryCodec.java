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
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.server.retry.model.RetryMessageModel;
import org.joyqueue.server.retry.remote.command.PutRetry;
import org.joyqueue.server.retry.util.RetrySerializerUtil;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 添加重试消息编解码
 *
 * Created by chengzhiliang on 2018/9/17.
 */
public class PutRetryCodec implements PayloadCodec<JoyQueueHeader, PutRetry>, Type {

    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        if (buffer == null) {
            return null;
        }

        int count = buffer.readShort(); // 读取重试消息条数
        List<RetryMessageModel> messageList = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            RetryMessageModel retryMessageModel = RetrySerializerUtil.deserialize(buffer);
            messageList.add(retryMessageModel);
        }

        PutRetry putRetry = new PutRetry(messageList);
        return putRetry;
    }

    @Override
    public void encode(PutRetry payload, ByteBuf buffer) throws Exception {
        List<RetryMessageModel> messageList = payload.getMessages();
        // 2字节条数
        if (messageList == null) {
            buffer.writeShort(0);
            return;
        }
        int size = messageList.size();
        buffer.writeShort(size);

        for (RetryMessageModel message : messageList) {
            ByteBuffer srcBuffer = RetrySerializerUtil.serialize(message);
            buffer.writeBytes(srcBuffer);
        }
    }

    @Override
    public int type() {
        return CommandType.PUT_RETRY;
    }
}
