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
package org.joyqueue.broker.kafka.util;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import org.joyqueue.broker.kafka.command.RawTaggedField;
import org.joyqueue.toolkit.security.Crc32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * Created by zhangkepeng on 16-8-18.
 */
public class KafkaBufferUtils {
    private static final Logger logger = LoggerFactory.getLogger(KafkaBufferUtils.class);

    /**
     * Write the given long value as a 4 byte unsigned integer. Overflow is ignored.
     *
     * @param buffer The buffer to write to
     * @param index  The position in the buffer at which to begin writing
     * @param value  The value to write
     */
    public static void writeUnsignedInt(ByteBuffer buffer, int index, long value) {
        buffer.putInt(index, (int) (value & 0xffffffffL));
    }

    /**
     * Read an unsigned integer from the given position without modifying the buffers
     * position
     *
     * @param buffer the buffer to read from
     * @param index  the index from which to read the integer
     * @return The integer read, as a long to avoid signedness
     */
    public static long readUnsignedInt(ByteBuffer buffer, int index) {
        return buffer.getInt(index) & 0xffffffffL;
    }

    /**
     * Read an integer stored in variable-length format using unsigned decoding from
     * <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html"> Google Protocol Buffers</a>.
     *
     * @param buffer The buffer to read from
     * @return The integer read
     *
     * @throws IllegalArgumentException if variable-length value does not terminate after 5 bytes have been read
     */
    public static int readUnsignedVarint(ByteBuffer buffer) {
        int value = 0;
        int i = 0;
        int b;
        while (((b = buffer.get()) & 0x80) != 0) {
            value |= (b & 0x7f) << i;
            i += 7;
            if (i > 28)
                throw new IllegalArgumentException(String.valueOf(value));
        }
        value |= b << i;
        return value;
    }

    public static int readUnsignedVarint(ByteBuf buffer) {
        int value = 0;
        int i = 0;
        int b;
        while (((b = buffer.readByte()) & 0x80) != 0) {
            value |= (b & 0x7f) << i;
            i += 7;
            if (i > 28)
                throw new IllegalArgumentException(String.valueOf(value));
        }
        value |= b << i;
        return value;
    }

    /**
     * Compute the CRC32 of the byte array
     *
     * @param bytes The array to compute the checksum for
     * @return The CRC32
     */
    public static long crc32(byte[] bytes) {
        return crc32(bytes, 0, bytes.length);
    }

    /**
     * Compute the CRC32 of the segment of the byte array given by the specificed size and offset
     *
     * @param bytes  The bytes to checksum
     * @param offset the offset at which to begin checksumming
     * @param size   the number of bytes to checksum
     * @return The CRC32
     */
    public static long crc32(byte[] bytes, int offset, int size) {
        Crc32 crc32 = new Crc32();
        crc32.update(bytes, offset, size);
        return crc32.getValue();
    }

    /**
     * read string
     *
     * @param buffer
     * @return
     */
    public static String readString(final ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes, Charsets.UTF_8);
    }

    /**
     * Write an unsigned integer in little-endian format to a byte array
     * at a given offset.
     *
     * @param buffer The byte array to write to
     * @param offset The position in buffer to write to
     * @param value  The value to write
     */
    public static void writeUnsignedIntLE(byte[] buffer, int offset, int value) {
        buffer[offset++] = (byte) (value >>> 8 * 0);
        buffer[offset++] = (byte) (value >>> 8 * 1);
        buffer[offset++] = (byte) (value >>> 8 * 2);
        buffer[offset] = (byte) (value >>> 8 * 3);
    }

    /**
     * Write an unsigned integer in little-endian format to the {@link java.io.OutputStream}.
     *
     * @param out   The stream to write to
     * @param value The value to write
     */
    public static void writeUnsignedIntLE(OutputStream out, int value) throws IOException {
        out.write(value >>> 8 * 0);
        out.write(value >>> 8 * 1);
        out.write(value >>> 8 * 2);
        out.write(value >>> 8 * 3);
    }

    /**
     * Read an unsigned integer stored in little-endian format from the {@link java.io.InputStream}.
     *
     * @param in The stream to read from
     * @return The integer read (MUST BE TREATED WITH SPECIAL CARE TO AVOID SIGNEDNESS)
     */
    public static int readUnsignedIntLE(InputStream in) throws IOException {
        return (in.read() << 8 * 0)
                | (in.read() << 8 * 1)
                | (in.read() << 8 * 2)
                | (in.read() << 8 * 3);
    }

    /**
     * Read an unsigned integer stored in little-endian format from a byte array
     * at a given offset.
     *
     * @param buffer The byte array to read from
     * @param offset The position in buffer to read from
     * @return The integer read (MUST BE TREATED WITH SPECIAL CARE TO AVOID SIGNEDNESS)
     */
    public static int readUnsignedIntLE(byte[] buffer, int offset) {
        return (buffer[offset++] << 8 * 0)
                | (buffer[offset++] << 8 * 1)
                | (buffer[offset++] << 8 * 2)
                | (buffer[offset] << 8 * 3);
    }

    public static void writeUnsignedLongLE(byte[] buffer, int offset, long value) {
        for (int i = 0; i < 8; i++) {
            buffer[i + offset] = ((byte) (value >>> (8 * i)));
        }
    }

    public static long readUnsignedLongLE(byte[] buffer, int offset) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result |= ((long) (buffer[i + offset]) & 0xff) << (8 * i);
        }
        return result;
    }

    /**
     * Create a new thread
     *
     * @param name     The name of the thread
     * @param runnable The work for the thread to do
     * @param daemon   Should the thread block JVM shutdown?
     * @return The unstarted thread
     */
    public static Thread newThread(String name, Runnable runnable, Boolean daemon) {
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(daemon);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                logger.error("Uncaught exception in thread '" + t.getName() + "':", e);
            }
        });
        return thread;
    }

    /**
     * Read the given byte buffer into a byte array
     */
    public static byte[] toArray(ByteBuffer buffer) {
        return toArray(buffer, 0, buffer.limit());
    }

    /**
     * Read a byte array from the given offset and size in the buffer
     */
    public static byte[] toArray(ByteBuffer buffer, int offset, int size) {
        byte[] dest = new byte[size];
        if (buffer.hasArray()) {
            System.arraycopy(buffer.array(), buffer.arrayOffset() + offset, dest, 0, size);
        } else {
            int pos = buffer.position();
            buffer.get(dest);
            buffer.position(pos);
        }
        return dest;
    }

    public static byte[] readBytes(ByteBuffer buffer) {
        int length = buffer.getInt();
        if (length <= 0) {
            return null;
        }
        byte[] result = new byte[length];
        buffer.get(result);
        return result;
    }

    public static byte[] readVarBytes(ByteBuffer buffer) {
        int length = readVarint(buffer);
        if (length <= 0) {
            return null;
        }
        byte[] result = new byte[length];
        buffer.get(result);
        return result;
    }

    public static byte[] readCompactBytes(ByteBuf buffer) {
        int length = readUnsignedVarint(buffer) - 1;
        if (length <= 0) {
            return null;
        }
        byte[] result = new byte[length];
        buffer.readBytes(result);
        return result;
    }

    public static String readCompactString(ByteBuf buffer) {
        byte[] content = readCompactBytes(buffer);
        if (content == null) {
            return null;
        }
        return new String(content);
    }

    public static void writeVarlong(long value, ByteBuf buffer) {
        long v = (value << 1) ^ (value >> 63);
        while ((v & 0xffffffffffffff80L) != 0L) {
            byte b = (byte) ((v & 0x7f) | 0x80);
            buffer.writeByte(b);
            v >>>= 7;
        }
        buffer.writeByte((byte) v);
    }

    public static void writeVarlong(long value, ByteBuffer buffer) {
        long v = (value << 1) ^ (value >> 63);
        while ((v & 0xffffffffffffff80L) != 0L) {
            byte b = (byte) ((v & 0x7f) | 0x80);
            buffer.put(b);
            v >>>= 7;
        }
        buffer.put((byte) v);
    }

    public static long readVarlong(ByteBuffer buffer)  {
        long value = 0L;
        int i = 0;
        long b;
        while (((b = buffer.get()) & 0x80) != 0) {
            value |= (b & 0x7f) << i;
            i += 7;
            if (i > 63) {
                throw new IllegalArgumentException();
            }
        }
        value |= b << i;
        return (value >>> 1) ^ -(value & 1);
    }

    public static void writeVarint(int value, ByteBuf buffer) {
        int v = (value << 1) ^ (value >> 31);
        while ((v & 0xffffff80) != 0L) {
            byte b = (byte) ((v & 0x7f) | 0x80);
            buffer.writeByte(b);
            v >>>= 7;
        }
        buffer.writeByte((byte) v);
    }

    public static void writeVarint(int value, ByteBuffer buffer) {
        int v = (value << 1) ^ (value >> 31);
        while ((v & 0xffffff80) != 0L) {
            byte b = (byte) ((v & 0x7f) | 0x80);
            buffer.put(b);
            v >>>= 7;
        }
        buffer.put((byte) v);
    }

    public static int readVarint(ByteBuffer buffer) {
        int value = 0;
        int i = 0;
        int b;
        while (((b = buffer.get()) & 0x80) != 0) {
            value |= (b & 0x7f) << i;
            i += 7;
            if (i > 28) {
                throw new IllegalArgumentException();
            }
        }
        value |= b << i;
        return (value >>> 1) ^ -(value & 1);
    }

    public static int readVarint(ByteBuf buffer) {
        int value = 0;
        int i = 0;
        int b;
        while (((b = buffer.readByte()) & 0x80) != 0) {
            value |= (b & 0x7f) << i;
            i += 7;
            if (i > 28) {
                throw new IllegalArgumentException();
            }
        }
        value |= b << i;
        return (value >>> 1) ^ -(value & 1);
    }

    public static void writeBytes(byte[] value, ByteBuf buffer) {
        if (value == null) {
            buffer.writeInt(-1);
        } else {
            buffer.writeInt(value.length);
            buffer.writeBytes(value);
        }
    }

    public static void writeVarBytes(byte[] value, ByteBuf buffer) {
        if (value == null) {
            writeVarint(-1, buffer);
        } else {
            writeVarint(value.length, buffer);
            buffer.writeBytes(value);
        }
    }

    public static List<RawTaggedField> readRawTaggedFields(ByteBuf buffer) {
        int size = readUnsignedVarint(buffer);
        if (size == 0) {
            return Collections.emptyList();
        }
        List<RawTaggedField> result = Lists.newLinkedList();
        for (int i = 0; i < size; i++) {
            int tag = readUnsignedVarint(buffer);
            int length = readUnsignedVarint(buffer);
            byte[] data = new byte[length];
            buffer.readBytes(length);
            result.add(new RawTaggedField(tag, data));
        }
        return result;
    }
}