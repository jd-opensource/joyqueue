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
package org.joyqueue.broker.kafka.message;

import org.joyqueue.broker.kafka.message.serializer.AbstractKafkaMessageSerializer;
import org.joyqueue.broker.kafka.message.serializer.KafkaMessageV0Serializer;
import org.joyqueue.broker.kafka.message.serializer.KafkaMessageV1Serializer;
import org.joyqueue.broker.kafka.message.serializer.KafkaMessageV2Serializer;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.message.SourceType;
import io.netty.buffer.ByteBuf;
import org.apache.commons.collections.CollectionUtils;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * KafkaMessageSerializer
 *
 * author: gaohaoxiang
 * date: 2018/11/9
 */
public class KafkaMessageSerializer extends AbstractKafkaMessageSerializer {

    public static void writeExtension(BrokerMessage brokerMessage, KafkaBrokerMessage kafkaBrokerMessage) {
        if (kafkaBrokerMessage.getMagic() == MESSAGE_MAGIC_V0) {
            KafkaMessageV0Serializer.writeExtension(brokerMessage, kafkaBrokerMessage);
        } else if (kafkaBrokerMessage.getMagic() == MESSAGE_MAGIC_V1) {
            KafkaMessageV1Serializer.writeExtension(brokerMessage, kafkaBrokerMessage);
        } else if (kafkaBrokerMessage.getMagic() == MESSAGE_MAGIC_V2) {
            KafkaMessageV2Serializer.writeExtension(brokerMessage, kafkaBrokerMessage);
        } else {
            throw new UnsupportedOperationException(String.format("writeExtension unsupported magic, magic: %s", kafkaBrokerMessage.getMagic()));
        }
    }

    public static void readExtension(BrokerMessage brokerMessage, KafkaBrokerMessage kafkaBrokerMessage) {
        if (brokerMessage.getSource() != SourceType.KAFKA.getValue()) {
            return;
        }

        byte magic = getExtensionMagic(brokerMessage.getExtension());
        kafkaBrokerMessage.setMagic(magic);
        if (magic == INVALID_EXTENSION_MAGIC) {
            return;
        }

        if (magic == MESSAGE_MAGIC_V0) {
            KafkaMessageV0Serializer.readExtension(brokerMessage, kafkaBrokerMessage);
        } else if (magic == MESSAGE_MAGIC_V1) {
            KafkaMessageV1Serializer.readExtension(brokerMessage, kafkaBrokerMessage);
        } else if (magic == MESSAGE_MAGIC_V2) {
            KafkaMessageV2Serializer.readExtension(brokerMessage, kafkaBrokerMessage);
        } else {
            throw new UnsupportedOperationException(String.format("readExtension unsupported magic, magic: %s", magic));
        }
    }

    public static void writeMessages(ByteBuf buffer, List<KafkaBrokerMessage> messages, short version) throws Exception {
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }

        byte supportedMagic = getSupportedMagic(version);

        for (KafkaBrokerMessage message : messages) {
            byte currentMagic = message.getMagic();
            byte requiredMagic = (byte) Math.min(currentMagic, supportedMagic);

            // 如果不支持V2, 但是当前版本是V2，需要先转换
            if (requiredMagic < MESSAGE_MAGIC_V2 && currentMagic == MESSAGE_MAGIC_V2) {
                List<KafkaBrokerMessage> batchMessages = KafkaMessageV2Serializer.readMessages(message);
                for (KafkaBrokerMessage batchMessage : batchMessages) {
                    writeMessage(buffer, batchMessage, requiredMagic);
                }
            } else {
                writeMessage(buffer, message, requiredMagic);
            }
        }
    }

    protected static byte getSupportedMagic(short version) {
        if (version <= 1) {
            return MESSAGE_MAGIC_V0;
        } else if (version <= 3) {
            return MESSAGE_MAGIC_V1;
        } else {
            return MESSAGE_MAGIC_V2;
        }
    }

    protected static void writeMessage(ByteBuf buffer, KafkaBrokerMessage message, byte magic) throws Exception {
        if (magic == MESSAGE_MAGIC_V0) {
            KafkaMessageV0Serializer.writeMessage(buffer, message);
        } else if (magic == MESSAGE_MAGIC_V1) {
            KafkaMessageV1Serializer.writeMessage(buffer, message);
        }  else if (magic == MESSAGE_MAGIC_V2) {
            KafkaMessageV2Serializer.writeMessage(buffer, message);
        } else if (magic == KafkaBrokerMessage.INVALID_MAGIC) {
            KafkaMessageV0Serializer.writeMessage(buffer, message);
        } else {
            throw new UnsupportedOperationException(String.format("writeMessage unsupported magic, magic: %s", magic));
        }
    }

    public static List<KafkaBrokerMessage> readMessages(ByteBuffer buffer) throws Exception {
        byte magic = buffer.get(MAGIC_OFFSET);
        if (magic == MESSAGE_MAGIC_V0) {
            return KafkaMessageV0Serializer.readMessages(buffer);
        } else if (magic == MESSAGE_MAGIC_V1) {
            return KafkaMessageV1Serializer.readMessages(buffer);
        } else if (magic == MESSAGE_MAGIC_V2) {
            return KafkaMessageV2Serializer.readMessages(buffer);
        } else {
            throw new UnsupportedOperationException(String.format("readMessages unsupported magic, magic: %s", magic));
        }
    }
}