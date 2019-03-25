package com.jd.journalq.broker.kafka.message.serializer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.broker.kafka.message.KafkaBrokerMessage;
import com.jd.journalq.broker.kafka.message.compressor.KafkaCompressionCodec;
import com.jd.journalq.broker.kafka.util.KafkaBufferUtils;
import com.jd.journalq.broker.kafka.util.PureJavaCrc32C;
import com.jd.journalq.message.BrokerMessage;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * KafkaMessageV2Serializer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/11
 */
public class KafkaMessageV2Serializer extends AbstractKafkaMessageSerializer {

    // magic
    private static final int EXTENSION_LENGTH = 1 + 8 + 8;

    private static final byte CURRENT_MAGIC = MESSAGE_MAGIC_V2;

    public static void writeExtension(BrokerMessage brokerMessage, KafkaBrokerMessage kafkaBrokerMessage) {
        byte[] extension = new byte[EXTENSION_LENGTH];
        writeExtensionMagic(extension, CURRENT_MAGIC);
        KafkaBufferUtils.writeUnsignedLongLE(extension, EXTENSION_CONTENT_OFFSET, kafkaBrokerMessage.getTimestamp());
        KafkaBufferUtils.writeUnsignedLongLE(extension, EXTENSION_ATTRIBUTE_OFFSET, kafkaBrokerMessage.getAttribute());
        brokerMessage.setExtension(extension);
    }

    public static void readExtension(BrokerMessage brokerMessage, KafkaBrokerMessage kafkaBrokerMessage) {
        byte[] extension = brokerMessage.getExtension();
        if (ArrayUtils.isEmpty(extension) || extension.length != EXTENSION_LENGTH) {
            return;
        }
        kafkaBrokerMessage.setMagic(CURRENT_MAGIC);
        kafkaBrokerMessage.setTimestamp(KafkaBufferUtils.readUnsignedLongLE(extension, EXTENSION_CONTENT_OFFSET));
        kafkaBrokerMessage.setAttribute((short) KafkaBufferUtils.readUnsignedLongLE(extension, EXTENSION_ATTRIBUTE_OFFSET));
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

        KafkaCompressionCodec compressionCodec = KafkaCompressionCodec.valueOf(getCompressionCodecType(attribute));
        KafkaBrokerMessage message = new KafkaBrokerMessage();
        message.setAttribute(attribute);
        message.setTimestamp(firstTimestamp);
        message.setOffset(baseOffset);
        message.setValue(value);
        message.setBatch(true);
        message.setMagic(CURRENT_MAGIC);
        message.setFlag((short) messageCount);
        message.setCompressionCodecType(compressionCodec.getCode());

        return Lists.newArrayList(message);
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
        message.setCompressionCodecType(KafkaCompressionCodec.NoCompressionCodec.getCode());

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