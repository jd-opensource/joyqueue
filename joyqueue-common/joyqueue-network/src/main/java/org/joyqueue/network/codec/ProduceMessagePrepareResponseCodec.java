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
import org.joyqueue.network.command.ProduceMessagePrepareResponse;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessagePrepareResponseCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/19
 */
public class ProduceMessagePrepareResponseCodec implements PayloadCodec<JoyQueueHeader, ProduceMessagePrepareResponse>, Type {

    @Override
    public ProduceMessagePrepareResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        ProduceMessagePrepareResponse produceMessagePrepareResponse = new ProduceMessagePrepareResponse();
        produceMessagePrepareResponse.setTxId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessagePrepareResponse.setCode(JoyQueueCode.valueOf(buffer.readInt()));
        return produceMessagePrepareResponse;
    }

    @Override
    public void encode(ProduceMessagePrepareResponse payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTxId(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(payload.getCode().getCode());
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_PREPARE_RESPONSE.getCode();
    }
}