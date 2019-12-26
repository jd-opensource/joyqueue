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
package org.joyqueue.broker.kafka.message.serializer;

import org.joyqueue.broker.kafka.message.compressor.KafkaCompressionCodec;
import org.joyqueue.broker.kafka.message.compressor.KafkaCompressionCodecFactory;
import org.joyqueue.broker.kafka.message.compressor.stream.ByteBufferInputStream;
import org.joyqueue.broker.kafka.util.KafkaBufferUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * AbstractKafkaMessageSerializer
 *
 * author: gaohaoxiang
 * date: 2018/11/11
 */
public abstract class AbstractKafkaMessageSerializer {

    public static final byte MESSAGE_MAGIC_V0 = 0;
    public static final byte MESSAGE_MAGIC_V1 = 1;
    public static final byte MESSAGE_MAGIC_V2 = 2;
    public static final byte MESSAGE_CURRENT_MAGIC = MESSAGE_MAGIC_V2;
    public static final byte INVALID_EXTENSION_MAGIC = -1;

    public static final int EXTENSION_MAGIC_OFFSET = 0;
    public static final int EXTENSION_TIMESTAMP_OFFSET = EXTENSION_MAGIC_OFFSET + 1;
    public static final int EXTENSION_ATTRIBUTE_OFFSET = EXTENSION_TIMESTAMP_OFFSET + 8;

    // v0, v1: offset + size + crc
    // v2:     offset + size + partitionLeaderEpoch
    public static final int OFFSET_OFFSET = 0;
    public static final int SIZE_OFFSET = OFFSET_OFFSET + 8;
    public static final int CRC_OFFSET = SIZE_OFFSET + 4;
    public static final int MAGIC_OFFSET = CRC_OFFSET + 4;
    public static final int ATTRIBUTE_OFFSET = MAGIC_OFFSET + 1;

    public static final byte COMPRESSION_CODEC_MASK = 0x07;
    public static final byte TRANSACTIONAL_FLAG_MASK = 0x10;
    public static final byte CONTROL_FLAG_MASK = 0x20;
    public static final byte TIMESTAMP_TYPE_MASK = 0x08;

    protected static final int DECOMPRESS_BUFFER_SIZE = 1024;

    public static byte getExtensionMagic(byte[] extension) {
        if (ArrayUtils.isEmpty(extension)) {
            return INVALID_EXTENSION_MAGIC;
        }
        return extension[EXTENSION_MAGIC_OFFSET];
    }

    public static void writeExtensionMagic(byte[] extension, byte magic) {
        extension[EXTENSION_MAGIC_OFFSET] = magic;
    }

    public static void writeExtensionTimestamp(byte[] extension, long timestamp) {
        KafkaBufferUtils.writeUnsignedLongLE(extension, EXTENSION_TIMESTAMP_OFFSET, timestamp);
    }

    public static void writeExtensionAttribute(byte[] extension, short attribute) {
        KafkaBufferUtils.writeUnsignedLongLE(extension, EXTENSION_ATTRIBUTE_OFFSET, attribute);
    }

    public static byte readExtensionMagic(byte[] extension) {
        return extension[EXTENSION_MAGIC_OFFSET];
    }

    public static long readExtensionTimestamp(byte[] extension) {
        return KafkaBufferUtils.readUnsignedLongLE(extension, EXTENSION_TIMESTAMP_OFFSET);
    }

    public static short readExtensionAttribute(byte[] extension) {
        return (short) KafkaBufferUtils.readUnsignedLongLE(extension, EXTENSION_ATTRIBUTE_OFFSET);
    }

    public static int getCompressionCodecType(short attribute) {
        return attribute & COMPRESSION_CODEC_MASK;
    }

    public static boolean isTransactionl(short attribute) {
        return (attribute & TRANSACTIONAL_FLAG_MASK) > 0;
    }

    public static int getTimestampType(short attribute) {
        return attribute & TIMESTAMP_TYPE_MASK;
    }

    public static byte[] decompress(KafkaCompressionCodec compressionCodec, ByteBuffer buffer, byte messageMagic) throws Exception {
        byte[] intermediateBuffer = new byte[DECOMPRESS_BUFFER_SIZE];
        ByteBufferInputStream sourceInputStream = new ByteBufferInputStream(buffer);
        InputStream inputStream = KafkaCompressionCodecFactory.apply(compressionCodec, sourceInputStream, messageMagic);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            int count;
            while ((count = inputStream.read(intermediateBuffer)) > 0) {
                outputStream.write(intermediateBuffer, 0, count);
            }
        } finally {
            inputStream.close();
        }
        return outputStream.toByteArray();
    }

    public static ByteBuffer decompressBuffer(KafkaCompressionCodec compressionCodec, ByteBuffer buffer, byte messageMagic) throws Exception {
        return ByteBuffer.wrap(decompress(compressionCodec, buffer, messageMagic));
    }
}