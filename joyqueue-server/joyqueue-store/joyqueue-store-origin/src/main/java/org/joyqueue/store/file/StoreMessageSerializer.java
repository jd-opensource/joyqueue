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
package org.joyqueue.store.file;

import org.joyqueue.store.PartialLogException;
import org.joyqueue.store.ReadException;
import org.joyqueue.store.message.MessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * @author liyue25
 * Date: 2018-11-28
 */
public class StoreMessageSerializer implements LogSerializer<ByteBuffer> {
    private static final Logger logger = LoggerFactory.getLogger(StoreMessageSerializer.class);
    private final long maxLogLength;

    public StoreMessageSerializer(long maxLogLength) {
        this.maxLogLength = maxLogLength;
    }

    private static boolean checkCRC(ByteBuffer buffer) {
        ByteBuffer body = MessageParser.getByteBuffer(buffer, MessageParser.BODY);
        if (body.remaining() > 0) {
            CRC32 crc32 = new CRC32();
            crc32.update(body);
            long crc = crc32.getValue();
            return crc == MessageParser.getLong(buffer, MessageParser.CRC);
        }
        return false;

    }

    private ByteBuffer read(ByteBuffer src) {

        ByteBuffer buffer;
        if (src.remaining() >= Integer.BYTES) {
            int length = src.getInt(src.position());
            buffer = readByLength(src, length);
            if (checkCRC(buffer)) {
                return buffer;
            }
        }
        throw new ReadException();

    }

    @Override
    public ByteBuffer read(ByteBuffer src, int length) {
        src.mark();
        try {
            if (length < 0) {
                return read(src);
            } else {
                return readByLength(src, length);
            }
        } catch (Throwable t) {
            src.reset();
            throw t;
        }
    }

    private ByteBuffer readByLength(ByteBuffer src, int length) {
        ByteBuffer buffer;
        if (length > Integer.BYTES && length < maxLogLength) {
            if (src.remaining() < length) throw new PartialLogException();
            byte[] readBuffer = new byte[length];
            src.get(readBuffer, 0, length);
            buffer = ByteBuffer.wrap(readBuffer);
            return buffer;
        }
        throw new ReadException();
    }

    /**
     * 从src中读取若干条Log，并返回这些Log的总长度
     *
     * @param src    存放消息的ByteBuffer，调用此方法不改变src的position、mark和limit
     * @param length 最多读取Log的总长度
     * @return 返回若干条消息，消息的条数不固定，但满足如下全部条件：
     * 1. Log数量不超过count
     * 2. Log总长度不超过length
     * 3. Log总长度不超过src剩余的字节数
     * 4. 返回所有Log的term相同
     */
    @Override
    public int trim(ByteBuffer src, int length) {
        ByteBuffer sliced = src.slice();
        int pos = 0;
        int lengthOfSrc = sliced.remaining();
        int vRemaining;
        int term = -1;
        while ((vRemaining = lengthOfSrc - pos) > MessageParser.getFixedAttributesLength()
                && pos < length) {
            int len = sliced.getInt(pos);
            if (len > MessageParser.getFixedAttributesLength() && len < maxLogLength) {
                if (vRemaining < len) {
                    break;
                }
                if (term < 0) {
                    term = sliced.getInt(pos + MessageParser.TERM);
                } else if (term != sliced.getInt(pos + MessageParser.TERM)) {
                    break;
                }
                pos += len;
            } else {
                logger.warn("Invalid length! length from message: {}, message offset: {}", len, pos);
                throw new ReadException();
            }
        }
        return pos;
    }

    @Override
    public int size(ByteBuffer buffer) {
        return buffer.remaining();
    }

    @Override
    public int append(ByteBuffer from, ByteBuffer to) {
        int length = from.remaining();
        from.mark();
        to.put(from);
        from.reset();
        return length;
    }

}
