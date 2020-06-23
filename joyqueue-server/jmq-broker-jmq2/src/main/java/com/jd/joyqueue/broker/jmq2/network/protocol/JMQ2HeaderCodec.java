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
package com.jd.joyqueue.broker.jmq2.network.protocol;

import com.jd.joyqueue.broker.jmq2.converter.QosConverter;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Header;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.domain.QosLevel;
import org.joyqueue.network.transport.codec.Codec;
import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;

/**
 * jmq协议头编解码器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class JMQ2HeaderCodec implements Codec {

    // VERSION + FLAG + REQUESTID + COMMANDTYPE + SENDTIME + STATUS + ERRORMSG
    //public static final int HEADER_LENGTH = 1 + 1 + 4 + 1 + 8 + 1 + 2;
    public static final int HEADER_LENGTH = 1 + 1 + 4 + 1 + 8;

    @Override
    public JMQ2Header decode(ByteBuf buffer) throws TransportException.CodecException {
        if (!buffer.isReadable(HEADER_LENGTH)) {
            return null;
        }

        byte version = buffer.readByte();
        byte identity = buffer.readByte();
        int requestId = buffer.readInt();
        int type = buffer.readUnsignedByte();
        long sendTime = buffer.readLong();
        short status = 0;
        String error = null;
        Direction direction = Direction.valueOf(identity & 0x1);
        // 将jmq2qos 转换成jmq4qos
        QosLevel qosLevel = QosConverter.toQosLevel((identity >> 1) & 0x3);

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

        return new JMQ2Header(version, qosLevel, direction, requestId, type, sendTime, status, error);
    }


    @Override
    public void encode(Object payload, ByteBuf buffer) throws TransportException.CodecException {
        JMQ2Header header = (JMQ2Header) payload;
        // 响应类型
        // 将jmq2qos 转换成jmq4qos
        byte identity = (byte) ((header.getDirection().ordinal() & 0x1) | ((QosConverter.toAcknowledge(header.getQosLevel().ordinal())).value() << 1 & 0x6));
        buffer.writeByte(header.getVersion());
        buffer.writeByte(identity);
        buffer.writeInt(header.getRequestId());
        buffer.writeByte(header.getType());
        buffer.writeLong(header.getTime());
        buffer.writeByte(header.getStatus());
        try {
            Serializer.write(header.getError(), buffer, 2);
        } catch (Exception e) {
            throw new TransportException.CodecException(e.getMessage());
        }
    }
}