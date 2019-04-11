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
package com.jd.journalq.network.codec;

import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.command.ProduceMessagePrepareAck;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessagePrepareAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessagePrepareAckCodec implements PayloadCodec<JMQHeader, ProduceMessagePrepareAck>, Type {

    @Override
    public ProduceMessagePrepareAck decode(JMQHeader header, ByteBuf buffer) throws Exception {
        ProduceMessagePrepareAck produceMessagePrepareAck = new ProduceMessagePrepareAck();
        produceMessagePrepareAck.setTxId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessagePrepareAck.setCode(JMQCode.valueOf(buffer.readInt()));
        return produceMessagePrepareAck;
    }

    @Override
    public void encode(ProduceMessagePrepareAck payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTxId(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(payload.getCode().getCode());
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_PREPARE_ACK.getCode();
    }
}