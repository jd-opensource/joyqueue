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

import com.google.common.collect.Lists;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.FetchProduceFeedbackAckData;
import org.joyqueue.network.command.FetchProduceFeedbackResponse;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * FetchProduceFeedbackResponseCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/19
 */
public class FetchProduceFeedbackResponseCodec implements PayloadCodec<JoyQueueHeader, FetchProduceFeedbackResponse>, Type {

    @Override
    public FetchProduceFeedbackResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        short dataSize = buffer.readShort();
        List<FetchProduceFeedbackAckData> data = Lists.newArrayListWithCapacity(dataSize);
        for (int i = 0; i < dataSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            String txId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            String transactionId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            data.add(new FetchProduceFeedbackAckData(topic, txId, transactionId));
        }

        FetchProduceFeedbackResponse fetchProduceFeedbackResponse = new FetchProduceFeedbackResponse();
        fetchProduceFeedbackResponse.setData(data);
        fetchProduceFeedbackResponse.setCode(JoyQueueCode.valueOf(buffer.readInt()));
        return fetchProduceFeedbackResponse;
    }

    @Override
    public void encode(FetchProduceFeedbackResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getData().size());
        for (FetchProduceFeedbackAckData data : payload.getData()) {
            Serializer.write(data.getTopic(), buffer, Serializer.SHORT_SIZE);
            Serializer.write(data.getTxId(), buffer, Serializer.SHORT_SIZE);
            Serializer.write(data.getTransactionId(), buffer, Serializer.SHORT_SIZE);
        }
        buffer.writeInt(payload.getCode().getCode());
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_PRODUCE_FEEDBACK_RESPONSE.getCode();
    }
}