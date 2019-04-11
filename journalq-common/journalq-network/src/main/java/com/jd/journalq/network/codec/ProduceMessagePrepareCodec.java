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

import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.command.ProduceMessagePrepare;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessagePrepareCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessagePrepareCodec implements PayloadCodec<JMQHeader, ProduceMessagePrepare>, Type {

    @Override
    public ProduceMessagePrepare decode(JMQHeader header, ByteBuf buffer) throws Exception {
        ProduceMessagePrepare produceMessagePrepare = new ProduceMessagePrepare();
        produceMessagePrepare.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessagePrepare.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessagePrepare.setSequence(buffer.readLong());
        produceMessagePrepare.setTransactionId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessagePrepare.setTimeout(buffer.readInt());
        return produceMessagePrepare;
    }

    @Override
    public void encode(ProduceMessagePrepare payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
        buffer.writeLong(payload.getSequence());
        Serializer.write(payload.getTransactionId(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(payload.getTimeout());
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_PREPARE.getCode();
    }
}