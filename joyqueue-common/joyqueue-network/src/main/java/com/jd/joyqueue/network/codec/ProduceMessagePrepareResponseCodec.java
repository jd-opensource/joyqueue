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
package com.jd.joyqueue.network.codec;

import com.jd.joyqueue.exception.JoyQueueCode;
import com.jd.joyqueue.network.command.JoyQueueCommandType;
import com.jd.joyqueue.network.command.ProduceMessagePrepareResponse;
import com.jd.joyqueue.network.serializer.Serializer;
import com.jd.joyqueue.network.transport.codec.JoyQueueHeader;
import com.jd.joyqueue.network.transport.codec.PayloadCodec;
import com.jd.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessagePrepareResponseCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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