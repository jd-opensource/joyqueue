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
package com.jd.journalq.broker.jmq.network.protocol.helper;

import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.JMQHeaderCodec;
import io.netty.buffer.ByteBuf;

/**
 * JMQProtocolHelper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/28
 */
public class JMQProtocolHelper {

    public static boolean isSupport(ByteBuf buffer) {
        if (buffer.readableBytes() < JMQHeaderCodec.HEADER_LENGTH) {
            return false;
        }

        int size = buffer.readInt();
        int magic = buffer.readInt();
        byte version = buffer.readByte();
        byte identity = buffer.readByte();
        int requestId = buffer.readInt();
        byte type = buffer.readByte();

        return (size >= JMQHeaderCodec.HEADER_LENGTH
                && magic == JMQHeader.MAGIC
                && version >= 0
                && identity >= 0
                && requestId >= 0
                && JMQCommandType.contains(type));
    }
}