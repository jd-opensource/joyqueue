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
package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.ClientProfileAck;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Header;
import com.jd.joyqueue.broker.jmq2.network.JMQ2PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * 客户端性能应答编码器
 */
public class ClientProfileAckCodec implements JMQ2PayloadCodec<ClientProfileAck>, Type {

    @Override
    public void encode(ClientProfileAck payload, ByteBuf out) throws Exception {
        payload.validate();
        out.writeInt(payload.getInterval());
    }

    @Override
    public Object decode(JMQ2Header header, ByteBuf buffer) throws Exception {
        return null;
    }

    @Override
    public int type() {
        return JMQ2CommandType.CLIENT_PROFILE_ACK.getCode();
    }
}