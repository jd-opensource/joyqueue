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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.broker.kafka.message.KafkaBrokerMessage;
import org.joyqueue.broker.kafka.message.compressor.KafkaCompressionCodec;
import org.joyqueue.broker.kafka.util.KafkaBufferUtils;
import org.joyqueue.broker.kafka.util.PureJavaCrc32C;
import org.joyqueue.message.BrokerMessage;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * KafkaMessageV2Serializer
 *
 * author: gaohaoxiang
 * date: 2018/11/11
 */
public class KafkaMessageV2Serializer extends AbstractKafkaMessageSerializer {

    private static final int EXTENSION_V0_LENGTH = 1; // magic
    private static final int EXTENSION_V1_LENGTH = 1 + 8 + 8; // magic + timestamp + attribute
    private static final int CURRENT_EXTENSION_LENGTH = EXTENSION_V1_LENGTH;

    private static final byte CURRENT_MAGIC = MESSAGE_MAGIC_V2;

    public static void writeExtension(BrokerMessage brokerMessage, KafkaBrokerMessage kafkaBrokerMessage) {
        byte[] extension = new byte[CURRENT_EXTENSION_LENGTH];
        writeExtensionMagic(extension, CURRENT_MAGIC);
        writeExtensionTimestamp(extension, kafkaBrokerMessage.getTimestamp());
        writeExtensionAttribute(extension, kafkaBrokerMessage.getAttribute());
        brokerMessage.setExtension(extension);
    }

    public static void readExtension(BrokerMessage brokerMessage, KafkaBrokerMessage kafkaBrokerMessage) {
        byte[] extension = brokerMessage.getExtension();
        if (ArrayUtils.isEmpty(extension)) {
            return;
        }

        if (extension.length == EXTENSION_V1_LENGTH) {
            kafkaBrokerMessage.setTimestamp(readExtensionTimestamp(extension));
            kafkaBrokerMessage.setAttribute(readExtensionAttribute(extension));
        }
    }

    public static void writeMessages(ByteBuf buffer, List<KafkaBrokerMessage> messages) throws Exception {
        for (KafkaBrokerMessage message : messages) {
            writeMessage(buffer, message);
        }
    }

    public static void writeMessage(ByteBuf buffer, KafkaBrokerMessage message) throws Exception {
        buffer.writeLong(message.getOffset()); // baseOffset
        int sizeIndex = buffer.writerIndex();
        buffer.writeInt(0); // size
        buffer.writeInt(-1); // partitionLeaderEpoch
        buffer.writeByte(CURRENT_MAGIC);
        int crcIndex = buffer.writerIndex();
        buffer.writeInt(0); // crc
        buffer.writeShort(message.getAttribute()); // attribute
        int offsetIndex = buffer.writerIndex();
        buffer.writeInt(message.getFlag() - 1); // lastOffsetDeltaLength
        buffer.writeLong(message.getTimestamp()); // firstTimestamp
        buffer.writeLong(message.getTimestamp()); // maxTimestamp
        buffer.writeLong(-1); // producerId
        buffer.writeShort(-1); // producerEpoch
        buffer.writeInt(0); // baseSequence
        buffer.writeInt(message.getFlag()); // messageCount

//        buffer.setInt(offsetIndex, (int) (lastMessage.getOffset() - firstMessage.getOffset()));
//        buffer.setLong(offsetIndex + 4 + 8, lastMessage.getTimestamp());

        buffer.writeBytes(message.getValue());

        // 计算整体长度
        int endIndex = buffer.writerIndex();
        buffer.setInt(sizeIndex, endIndex - sizeIndex - 4);

        // 计算crc，从crc开始后的全部数据
        byte[] bytes = new byte[endIndex - crcIndex - 4];
        buffer.getBytes(crcIndex + 4, bytes);

        PureJavaCrc32C crc32c = new PureJavaCrc32C();
        crc32c.update(bytes, 0, bytes.length);
        long crc = crc32c.getValue();
        buffer.setInt(crcIndex, (int) (crc & 0xffffffffL));
    }

    public static List<KafkaBrokerMessage> readMessages(ByteBuffer buffer) throws Exception {
        long baseOffset = buffer.getLong();
        int size = buffer.getInt();
        int partitionLeaderEpoch = buffer.getInt();
        byte magic = buffer.get();
        int crc = buffer.getInt();
        short attribute = buffer.getShort();
        int lastOffsetDeltaLength = buffer.getInt();
        long firstTimestamp = buffer.getLong();
        long maxTimestamp = buffer.getLong();
        long producerId = buffer.getLong();
        short producerEpoch = buffer.getShort();
        int baseSequence = buffer.getInt();
        int messageCount = buffer.getInt();

        byte[] value = new byte[buffer.remaining()];
        buffer.get(value);

//        KafkaCompressionCodec compressionCodec = KafkaCompressionCodec.valueOf(getCompressionCodecType(attribute));
        KafkaBrokerMessage message = new KafkaBrokerMessage();
        message.setMagic(magic);
        message.setAttribute(attribute);
        message.setTimestamp(firstTimestamp);
        message.setOffset(baseOffset);
        message.setValue(value);
        message.setBatch(true);
        message.setMagic(CURRENT_MAGIC);
        message.setFlag((short) messageCount);
        message.setCrc(crc);

        message.setTransaction(isTransactionl(attribute));
        message.setProducerId(producerId);
        message.setProducerEpoch(producerEpoch);
        message.setBaseSequence(baseSequence);

        return Lists.newArrayList(message);
    }

    public static List<KafkaBrokerMessage> readMessages(KafkaBrokerMessage message) throws Exception {
        short attribute = message.getAttribute();
        KafkaCompressionCodec compressionType = KafkaCompressionCodec.valueOf(getCompressionCodecType(attribute));
        byte[] body = message.getValue();

        if (!compressionType.equals(KafkaCompressionCodec.NoCompressionCodec)) {
            body = decompress(compressionType, ByteBuffer.wrap(body), CURRENT_MAGIC);
        }

        ByteBuffer bodyBuffer = ByteBuffer.wrap(body);
        List<KafkaBrokerMessage> result = Lists.newArrayListWithCapacity(message.getFlag());

        for (int i = 0; i < message.getFlag(); i++) {
            KafkaBrokerMessage brokerMessage = readMessage(message, bodyBuffer);
            result.add(brokerMessage);
        }
        return result;
    }

    protected static KafkaBrokerMessage readMessage(KafkaBrokerMessage message, ByteBuffer buffer) throws Exception {
        KafkaBrokerMessage result = new KafkaBrokerMessage();
        result.setSize(KafkaBufferUtils.readVarint(buffer));
        buffer.get(); // attribute
        long timestamp = KafkaBufferUtils.readVarlong(buffer); // timestamp
        result.setOffset(KafkaBufferUtils.readVarint(buffer) + message.getOffset());

        byte[] businessId = KafkaBufferUtils.readVarBytes(buffer);
        result.setKey((ArrayUtils.isEmpty(businessId) ? null : businessId));
        result.setValue(KafkaBufferUtils.readVarBytes(buffer));
        result.setTimestamp(timestamp + message.getTimestamp());

        result.setAttribute((short) 0);
        result.setFlag(message.getFlag());
        result.setBatch(true);

        int headerCount = KafkaBufferUtils.readVarint(buffer);
        if (headerCount != 0) {
            Map<byte[], byte[]> headers = Maps.newHashMap();
            for (int i = 0; i < headerCount; i++) {
                headers.put(KafkaBufferUtils.readVarBytes(buffer), KafkaBufferUtils.readVarBytes(buffer));
            }
            result.setHeader(headers);
        }

        return result;
    }

    public static KafkaBrokerMessage readMessage(long baseOffset, long firstTimestamp, ByteBuffer buffer) throws Exception {
        KafkaBrokerMessage message = new KafkaBrokerMessage();
        message.setSize(KafkaBufferUtils.readVarint(buffer));
        message.setAttribute(buffer.get());
        message.setTimestamp(KafkaBufferUtils.readVarlong(buffer) + firstTimestamp);
        message.setOffset(KafkaBufferUtils.readVarint(buffer) + baseOffset);
        message.setKey(KafkaBufferUtils.readVarBytes(buffer));
        message.setValue(KafkaBufferUtils.readVarBytes(buffer));
        message.setBatch(true);
        message.setMagic(CURRENT_MAGIC);
        message.setCrc(0);

        int headerCount = KafkaBufferUtils.readVarint(buffer);
        if (headerCount != 0) {
            Map<byte[], byte[]> headers = Maps.newHashMap();
            for (int i = 0; i < headerCount; i++) {
                byte[] headerKey = KafkaBufferUtils.readVarBytes(buffer);
                byte[] headerValue = KafkaBufferUtils.readVarBytes(buffer);
                headers.put(headerKey, headerValue);
            }
            message.setHeader(headers);
        }

        return message;
    }
}