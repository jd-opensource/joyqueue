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
package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.HasSubscribeAck;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class HasSubscribeAckCodec implements NsrPayloadCodec<HasSubscribeAck>, Type {

    @Override
    public HasSubscribeAck decode(Header header, ByteBuf buffer) throws Exception {
        return new HasSubscribeAck().have(buffer.readBoolean());
    }

    @Override
    public void encode(HasSubscribeAck payload, ByteBuf buffer) throws Exception {
        buffer.writeBoolean(payload.isHave());
    }

    @Override
    public int type() {
        return NsrCommandType.HAS_SUBSCRIBE_ACK;
    }
}
