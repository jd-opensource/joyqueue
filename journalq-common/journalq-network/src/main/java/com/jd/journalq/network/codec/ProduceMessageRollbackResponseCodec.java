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

import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.command.ProduceMessageRollbackResponse;
import com.jd.journalq.network.transport.codec.JournalqHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessageRollbackResponseCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessageRollbackResponseCodec implements PayloadCodec<JournalqHeader, ProduceMessageRollbackResponse>, Type {

    @Override
    public ProduceMessageRollbackResponse decode(JournalqHeader header, ByteBuf buffer) throws Exception {
        ProduceMessageRollbackResponse produceMessageRollbackResponse = new ProduceMessageRollbackResponse();
        produceMessageRollbackResponse.setCode(JournalqCode.valueOf(buffer.readInt()));
        return produceMessageRollbackResponse;
    }

    @Override
    public void encode(ProduceMessageRollbackResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getCode().getCode());
    }

    @Override
    public int type() {
        return JournalqCommandType.PRODUCE_MESSAGE_ROLLBACK_RESPONSE.getCode();
    }
}