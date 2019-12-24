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
package org.joyqueue.client.internal.consumer.converter.kafka;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.client.internal.consumer.converter.MessageConverter;
import org.joyqueue.client.internal.consumer.converter.kafka.compressor.KafkaCompressionCodec;
import org.joyqueue.client.internal.consumer.converter.kafka.compressor.KafkaCompressionCodecFactory;
import org.joyqueue.client.internal.consumer.converter.kafka.compressor.stream.ByteBufferInputStream;
import org.joyqueue.client.internal.exception.ClientException;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.message.SourceType;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * KafkaMessageConverter
 *
 * author: gaohaoxiang
 * date: 2019/4/3
 */
public class KafkaMessageConverter implements MessageConverter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final byte MESSAGE_MAGIC_V0 = 0;
    private static final byte MESSAGE_MAGIC_V1 = 1;
    private static final byte MESSAGE_MAGIC_V2 = 2;
    private static final byte MESSAGE_CURRENT_MAGIC = MESSAGE_MAGIC_V2;

    private static final int EXTENSION_V0_LENGTH = 1; // magic
    private static final int EXTENSION_V1_LENGTH = 1 + 8 + 8; // magic + timestamp + attribute
    private static final int EXTENSION_CURRENT_LENGTH = EXTENSION_V1_LENGTH;

    private static final int EXTENSION_BATCH_V0_LENGTH = 1; // magic
    private static final int EXTENSION_BATCH_V1_LENGTH = 1 + 8 + 8; // magic + timestamp + attribute
    private static final int EXTENSION_CURRENT_BATCH_LENGTH = EXTENSION_BATCH_V1_LENGTH;

    private static final int EXTENSION_MAGIC_OFFSET = 0;
    private static final int EXTENSION_TIMESTAMP_OFFSET = EXTENSION_MAGIC_OFFSET + 1;
    private static final int EXTENSION_ATTRIBUTE_OFFSET = EXTENSION_TIMESTAMP_OFFSET + 8;

    private static final byte COMPRESSION_CODEC_MASK = 0x07;

    private static final int DECOMPRESS_BUFFER_SIZE = 1024;

    @Override
    public BrokerMessage convert(BrokerMessage message) {
        byte[] extension = message.getExtension();
        if (ArrayUtils.isEmpty(extension) || extension.length != EXTENSION_CURRENT_LENGTH) {
            return message;
        }

        byte messageMagic = extension[EXTENSION_MAGIC_OFFSET];
        short attribute = (short) KafkaBufferUtils.readUnsignedLongLE(extension, EXTENSION_ATTRIBUTE_OFFSET);
        KafkaCompressionCodec compressionCodec = KafkaCompressionCodec.valueOf(attribute & COMPRESSION_CODEC_MASK);

        if (!compressionCodec.equals(KafkaCompressionCodec.NoCompressionCodec)) {
            byte[] body = decompress(compressionCodec, message.getBody(), messageMagic);
            message = readDecompressedMessage(ByteBuffer.wrap(body), message, messageMagic);
        }

        return message;
    }

    protected BrokerMessage readDecompressedMessage(ByteBuffer buffer, BrokerMessage message, byte messageMagic) {
        buffer.getLong(); // offset
        buffer.getInt(); // size
        buffer.getInt(); // crc
        buffer.get(); // magic
        buffer.get(); // attribute
        if (messageMagic >= MESSAGE_MAGIC_V1) {
            buffer.getLong(); // timestamp
        }
        byte[] key = KafkaBufferUtils.readBytes(buffer);
        byte[] value = KafkaBufferUtils.readBytes(buffer);
        message.setBusinessId(ArrayUtils.isNotEmpty(key) ? new String(key, Charsets.UTF_8) : null);
        message.setBody(ArrayUtils.isNotEmpty(value) ? value : null);
        return message;
    }

    @Override
    public List<BrokerMessage> convertBatch(BrokerMessage message) {
        List<BrokerMessage> result = Lists.newArrayListWithCapacity(message.getFlag());
        byte[] body = tryDecompress(message);
        ByteBuffer buffer = ByteBuffer.wrap(body);

        for (int i = 0; i < message.getFlag(); i++) {
            result.add(doConvertBatch(message, buffer, i));
        }
        return result;
    }

    protected byte[] tryDecompress(BrokerMessage message) {
        byte[] extension = message.getExtension();
        if (ArrayUtils.isEmpty(extension) || extension.length != EXTENSION_CURRENT_BATCH_LENGTH) {
            return message.getByteBody();
        }

        byte messageMagic = extension[EXTENSION_MAGIC_OFFSET];
        short attribute = (short) KafkaBufferUtils.readUnsignedLongLE(extension, EXTENSION_ATTRIBUTE_OFFSET);
        int compressionType = attribute & COMPRESSION_CODEC_MASK;

        KafkaCompressionCodec kafkaCompressionCodec = KafkaCompressionCodec.valueOf(compressionType);
        if (kafkaCompressionCodec.equals(KafkaCompressionCodec.NoCompressionCodec)) {
            return message.getByteBody();
        }

        return decompress(kafkaCompressionCodec, ByteBuffer.wrap(message.getByteBody()), messageMagic);
    }

    protected byte[] decompress(KafkaCompressionCodec kafkaCompressionCodec, ByteBuffer buffer, byte messageMagic) {
        try {
            byte[] intermediateBuffer = new byte[DECOMPRESS_BUFFER_SIZE];
            ByteBufferInputStream sourceInputStream = new ByteBufferInputStream(buffer);
            InputStream inputStream = KafkaCompressionCodecFactory.apply(kafkaCompressionCodec, sourceInputStream, messageMagic);
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
        } catch (Exception e) {
            logger.error("decompress exception, kafkaCompressionCodec: {}, messageMagic: {}", kafkaCompressionCodec, messageMagic, e);
            throw new ClientException(e);
        }
    }

    protected BrokerMessage doConvertBatch(BrokerMessage message, ByteBuffer buffer, int index) {
        BrokerMessage result = new BrokerMessage();
        result.setSize(KafkaBufferUtils.readVarint(buffer));
        buffer.get(); // attribute
        KafkaBufferUtils.readVarlong(buffer); // timestamp
//        result.setMsgIndexNo(KafkaBufferUtils.readVarint(buffer) + message.getMsgIndexNo());
        KafkaBufferUtils.readVarint(buffer); // offset
        result.setMsgIndexNo(index + message.getMsgIndexNo());

        byte[] businessId = KafkaBufferUtils.readVarBytes(buffer);
        result.setTopic(message.getTopic());
        result.setBusinessId((ArrayUtils.isEmpty(businessId) ? null : new String(businessId, Charsets.UTF_8)));
        result.setBody(KafkaBufferUtils.readVarBytes(buffer));

        result.setApp(message.getApp());
        result.setPartition(message.getPartition());
        result.setAttributes(message.getAttributes());
        result.setStartTime(message.getStartTime());
        result.setFlag(message.getFlag());
        result.setSource(SourceType.KAFKA.getValue());
        result.setClientIp(message.getClientIp());
        result.setPriority(message.getPriority());
        result.setOrdered(message.isOrdered());
        result.setStartTime(message.getStartTime());
        result.setStoreTime(message.getStoreTime());
        result.setCompressed(false);
        result.setBatch(true);

        int headerCount = KafkaBufferUtils.readVarint(buffer);
        if (headerCount > 0) {
            Map<String, String> headers = Maps.newHashMap();
            for (int i = 0; i < headerCount; i++) {
                String key = new String(KafkaBufferUtils.readVarBytes(buffer), Charsets.UTF_8);
                String value = new String(KafkaBufferUtils.readVarBytes(buffer), Charsets.UTF_8);
                headers.put(key, value);
            }
            result.setAttributes(headers);
        }

        return result;
    }

    @Override
    public Byte type() {
        return SourceType.KAFKA.getValue();
    }
}