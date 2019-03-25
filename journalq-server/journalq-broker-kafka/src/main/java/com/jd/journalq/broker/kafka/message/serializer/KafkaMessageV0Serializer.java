package com.jd.journalq.broker.kafka.message.serializer;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.kafka.message.KafkaBrokerMessage;
import com.jd.journalq.broker.kafka.message.compressor.KafkaCompressionCodec;
import com.jd.journalq.broker.kafka.util.KafkaBufferUtils;
import com.jd.journalq.common.message.BrokerMessage;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * KafkaMessageV0Serializer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/11
 */
public class KafkaMessageV0Serializer extends AbstractKafkaMessageSerializer {

    // magic
    private static final int EXTENSION_LENGTH = 1;

    private static final byte CURRENT_MAGIC = MESSAGE_MAGIC_V0;

    public static void writeExtension(BrokerMessage brokerMessage, KafkaBrokerMessage kafkaBrokerMessage) {
        byte[] extension = new byte[EXTENSION_LENGTH];
        writeExtensionMagic(extension, CURRENT_MAGIC);
        brokerMessage.setExtension(extension);
    }

    public static void readExtension(BrokerMessage brokerMessage, KafkaBrokerMessage kafkaBrokerMessage) {
        byte[] extension = brokerMessage.getExtension();
        if (ArrayUtils.isEmpty(extension) || extension.length != EXTENSION_LENGTH) {
            return;
        }
        kafkaBrokerMessage.setMagic(CURRENT_MAGIC);
    }

    public static void writeMessages(ByteBuf buffer, List<KafkaBrokerMessage> messages) throws Exception {
        for (KafkaBrokerMessage message : messages) {
            writeMessage(buffer, message);
        }
    }

    public static void writeMessage(ByteBuf buffer, KafkaBrokerMessage message) throws Exception {
        buffer.writeLong(message.getOffset());

        int startIndex = buffer.writerIndex();
        buffer.writeInt(0); // length
        buffer.writeInt(0); // crc
        buffer.writeByte(message.getMagic());
        buffer.writeByte(message.getAttribute());

        KafkaBufferUtils.writeBytes(message.getKey(), buffer);
        KafkaBufferUtils.writeBytes(message.getValue(), buffer);

        buffer.writeInt(message.getValue().length);
        buffer.writeBytes(message.getValue());

        // 计算整个message长度，包括长度本身的字节数
        int length = buffer.writerIndex() - startIndex;
        byte[] bytes = new byte[length];
        buffer.getBytes(startIndex, bytes);

        // 计算crc，不包括长度字节和crc字节，从magic开始
        long crc = KafkaBufferUtils.crc32(bytes, 4 + 4, bytes.length - 4 - 4);

        // 写入长度和crc
        buffer.markWriterIndex();
        buffer.writerIndex(startIndex);
        buffer.writeInt(length - 4);
        buffer.writeInt((int) (crc & 0xffffffffL));
        buffer.resetWriterIndex();
    }

    public static List<KafkaBrokerMessage> readMessages(ByteBuffer buffer) throws Exception {
        List<KafkaBrokerMessage> result = Lists.newLinkedList();
        while (buffer.hasRemaining()) {
            KafkaBrokerMessage message = readMessage(buffer);
            result.add(message);
        }
        return result;
    }

    public static KafkaBrokerMessage readMessage(ByteBuffer buffer) throws Exception {
        byte attribute = buffer.get(ATTRIBUTE_OFFSET);
        KafkaCompressionCodec compressionCodec = KafkaCompressionCodec.valueOf(getCompressionCodecType(attribute));
        if (!compressionCodec.equals(KafkaCompressionCodec.NoCompressionCodec)) {
            buffer.position(ATTRIBUTE_OFFSET + 1 + 4 + 4); // attribute, key, valueLength
            buffer = decompress(compressionCodec, buffer, CURRENT_MAGIC);
        }
        return doReadMessage(buffer);
    }

    protected static KafkaBrokerMessage doReadMessage(ByteBuffer buffer) throws Exception {
        KafkaBrokerMessage message = new KafkaBrokerMessage();
        message.setOffset(buffer.getLong());
        message.setSize(buffer.getInt());
        message.setCrc(buffer.getInt());
        message.setMagic(buffer.get());
        message.setAttribute(buffer.get());
        message.setCompressionCodecType(getCompressionCodecType(message.getAttribute()));
        message.setKey(KafkaBufferUtils.readBytes(buffer));
        message.setValue(KafkaBufferUtils.readBytes(buffer));
        return message;
    }
}