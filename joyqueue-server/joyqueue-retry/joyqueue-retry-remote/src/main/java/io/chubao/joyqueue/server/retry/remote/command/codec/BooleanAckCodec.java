/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.server.retry.remote.command.codec;

import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * Created by chengzhiliang on 2018/10/12.
 */
public class BooleanAckCodec implements PayloadCodec<JoyQueueHeader, BooleanAck>, Type {

    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        // 布尔应答不解析消息体
        return null;
    }

    @Override
    public void encode(BooleanAck payload, ByteBuf buffer) throws Exception {
        // 布尔应答编码消息体
    }

    @Override
    public int type() {
        return CommandType.BOOLEAN_ACK;
    }
}
