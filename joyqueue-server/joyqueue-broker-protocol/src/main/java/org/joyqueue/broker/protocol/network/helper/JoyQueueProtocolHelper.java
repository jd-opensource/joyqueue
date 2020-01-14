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
package org.joyqueue.broker.protocol.network.helper;

import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.JoyQueueHeaderCodec;
import io.netty.buffer.ByteBuf;

/**
 * JoyQueueProtocolHelper
 *
 * author: gaohaoxiang
 * date: 2018/11/28
 */
public class JoyQueueProtocolHelper {

    public static boolean isSupport(ByteBuf buffer) {
        if (buffer.readableBytes() < JoyQueueHeaderCodec.HEADER_LENGTH) {
            return false;
        }

        int size = buffer.readInt();
        int magic = buffer.readInt();
        byte version = buffer.readByte();
        byte identity = buffer.readByte();
        int requestId = buffer.readInt();
        byte type = buffer.readByte();

        return (size >= JoyQueueHeaderCodec.HEADER_LENGTH
                && magic == JoyQueueHeader.MAGIC
                && version >= 0
                && identity >= 0
                && JoyQueueCommandType.contains(type));
    }
}