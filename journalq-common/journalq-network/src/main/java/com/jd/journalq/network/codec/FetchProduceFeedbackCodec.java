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

import com.jd.journalq.network.command.FetchProduceFeedback;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.command.TxStatus;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * FetchProduceFeedbackCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class FetchProduceFeedbackCodec implements PayloadCodec<JMQHeader, FetchProduceFeedback>, Type {

    @Override
    public FetchProduceFeedback decode(JMQHeader header, ByteBuf buffer) throws Exception {
        FetchProduceFeedback fetchProduceFeedback = new FetchProduceFeedback();
        fetchProduceFeedback.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        fetchProduceFeedback.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        fetchProduceFeedback.setStatus(TxStatus.valueOf(buffer.readByte()));
        fetchProduceFeedback.setLongPollTimeout(buffer.readInt());
        return fetchProduceFeedback;
    }

    @Override
    public void encode(FetchProduceFeedback payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getTopic(), buffer, Serializer.SHORT_SIZE);
        buffer.writeByte(payload.getStatus().getType());
        buffer.writeInt(payload.getLongPollTimeout());
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_PRODUCE_FEEDBACK.getCode();
    }
}