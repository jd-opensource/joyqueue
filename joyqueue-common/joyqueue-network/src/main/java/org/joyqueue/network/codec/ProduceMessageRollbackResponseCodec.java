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
package org.joyqueue.network.codec;

import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.command.ProduceMessageRollbackResponse;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessageRollbackResponseCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/19
 */
public class ProduceMessageRollbackResponseCodec implements PayloadCodec<JoyQueueHeader, ProduceMessageRollbackResponse>, Type {

    @Override
    public ProduceMessageRollbackResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        ProduceMessageRollbackResponse produceMessageRollbackResponse = new ProduceMessageRollbackResponse();
        produceMessageRollbackResponse.setCode(JoyQueueCode.valueOf(buffer.readInt()));
        return produceMessageRollbackResponse;
    }

    @Override
    public void encode(ProduceMessageRollbackResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getCode().getCode());
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_ROLLBACK_RESPONSE.getCode();
    }
}