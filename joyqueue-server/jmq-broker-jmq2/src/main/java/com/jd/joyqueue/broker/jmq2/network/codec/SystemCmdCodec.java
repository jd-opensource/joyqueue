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
import com.jd.joyqueue.broker.jmq2.command.SystemCmd;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.network.transport.codec.PayloadDecoder;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * 系统命令解码器
 */
public class SystemCmdCodec implements PayloadDecoder, Type {

    @Override
    public Object decode(Header header, ByteBuf in) throws Exception {
        SystemCmd payload = new SystemCmd();
        payload.setCmd(Serializer.readString(in, 1));
        payload.setUrl(Serializer.readString(in, 2));
        payload.setTimeout(in.readInt());
        return payload;
    }

    @Override
    public int type() {
        return JMQ2CommandType.SYSTEM_COMMAND.getCode();
    }
}