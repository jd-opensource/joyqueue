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
package org.joyqueue.network.transport.codec;

import org.joyqueue.domain.QosLevel;
import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;

/**
 * JoyQueueHeaderCodec
 *
 * author: gaohaoxiang
 * date: 2018/8/21
 */
public class JoyQueueHeaderCodec implements Codec {

    // MAGIC + VERSION + FLAG + REQUESTID + COMMANDTYPE + SENDTIME + STATUS + ERRORMSG
    public static final int HEADER_LENGTH = 4 + 1 + 1 + 4 + 1 + 8;

    @Override
    public JoyQueueHeader decode(ByteBuf buffer) throws TransportException.CodecException {
        if (buffer.readableBytes() < HEADER_LENGTH) {
            return null;
        }

        int magic = buffer.readInt();
        if (magic != JoyQueueHeader.MAGIC) {
            return null;
        }

        byte version = buffer.readByte();
        byte identity = buffer.readByte();
        int requestId = buffer.readInt();
        byte type = buffer.readByte();
        long sendTime = buffer.readLong();
        short status = 0;
        String error = null;
        Direction direction = Direction.valueOf(identity & 0x1);
        QosLevel qosLevel = QosLevel.valueOf((identity >> 1) & 0x3);

        if (direction.equals(Direction.RESPONSE)) {
            // 1个字节的状态码
            status = buffer.readUnsignedByte();
            // 2个字节的异常长度
            // 异常信息
            try {
                error = Serializer.readString(buffer, 2);
            } catch (Exception e) {
                throw new TransportException.CodecException(e.getMessage());
            }
        }

        return new JoyQueueHeader(version, qosLevel, direction, requestId, type, sendTime, status, error);
    }

    @Override
    public void encode(Object payload, ByteBuf buffer) throws TransportException.CodecException {
        JoyQueueHeader header = (JoyQueueHeader) payload;
        // 响应类型
        byte identity = (byte) ((header.getDirection().ordinal() & 0x1) | ((header.getQosLevel().ordinal()) << 1 & 0x6));

        buffer.writeInt(JoyQueueHeader.MAGIC);
        buffer.writeByte(header.getVersion());
        buffer.writeByte(identity);
        buffer.writeInt(header.getRequestId());
        buffer.writeByte(header.getType());
        buffer.writeLong(header.getTime());
        if (header.getDirection().equals(Direction.RESPONSE)) {
            buffer.writeByte(header.getStatus());
            try {
                Serializer.write(header.getError(), buffer, 2);
            } catch (Exception e) {
                throw new TransportException.CodecException(e.getMessage());
            }
        }
    }
}