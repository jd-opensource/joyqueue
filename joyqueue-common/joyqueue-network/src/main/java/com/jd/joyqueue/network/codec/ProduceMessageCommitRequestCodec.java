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

import com.jd.joyqueue.network.command.JournalqCommandType;
import com.jd.joyqueue.network.command.ProduceMessageCommitRequest;
import com.jd.joyqueue.network.serializer.Serializer;
import com.jd.joyqueue.network.transport.codec.JournalqHeader;
import com.jd.joyqueue.network.transport.codec.PayloadCodec;
import com.jd.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessageCommitRequestCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessageCommitRequestCodec implements PayloadCodec<JournalqHeader, ProduceMessageCommitRequest>, Type {

    @Override
    public ProduceMessageCommitRequest decode(JournalqHeader header, ByteBuf buffer) throws Exception {
        ProduceMessageCommitRequest produceMessageCommitRequest = new ProduceMessageCommitRequest();
        produceMessageCommitRequest.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessageCommitRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessageCommitRequest.setTxId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return produceMessageCommitRequest;
    }

    @Override
    public void encode(ProduceMessageCommitRequest payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getTxId(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JournalqCommandType.PRODUCE_MESSAGE_COMMIT_REQUEST.getCode();
    }
}