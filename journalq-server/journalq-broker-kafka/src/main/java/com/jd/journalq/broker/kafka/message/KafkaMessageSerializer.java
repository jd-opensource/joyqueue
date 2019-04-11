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
package com.jd.journalq.broker.kafka.message;

import com.jd.journalq.broker.kafka.message.serializer.AbstractKafkaMessageSerializer;
import com.jd.journalq.broker.kafka.message.serializer.KafkaMessageV0Serializer;
import com.jd.journalq.broker.kafka.message.serializer.KafkaMessageV1Serializer;
import com.jd.journalq.broker.kafka.message.serializer.KafkaMessageV2Serializer;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.message.SourceType;
import io.netty.buffer.ByteBuf;
import org.apache.commons.collections.CollectionUtils;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * KafkaMessageSerializer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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

    public static void writeMessages(ByteBuf buffer, List<KafkaBrokerMessage> messages) throws Exception {
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        // TODO 临时处理，需要根据协议版本决定
        byte magic = messages.get(0).getMagic();
        if (magic == MESSAGE_MAGIC_V0) {
            KafkaMessageV0Serializer.writeMessages(buffer, messages);
        } else if (magic == MESSAGE_MAGIC_V1) {
            KafkaMessageV1Serializer.writeMessages(buffer, messages);
        }  else if (magic == MESSAGE_MAGIC_V2) {
            KafkaMessageV2Serializer.writeMessages(buffer, messages);
        } else if (magic == KafkaBrokerMessage.INVALID_MAGIC) {
            KafkaMessageV0Serializer.writeMessages(buffer, messages);
        } else {
            throw new UnsupportedOperationException(String.format("writeMessage unsupported magic, magic: %s", magic));
        }
    }

    public static void writeMessage(ByteBuf buffer, KafkaBrokerMessage message) throws Exception {
        if (message.getMagic() == MESSAGE_MAGIC_V0) {
            KafkaMessageV0Serializer.writeMessage(buffer, message);
        } else if (message.getMagic() == MESSAGE_MAGIC_V1) {
            KafkaMessageV1Serializer.writeMessage(buffer, message);
        } else if (message.getMagic() == KafkaBrokerMessage.INVALID_MAGIC) {
            KafkaMessageV0Serializer.writeMessage(buffer, message);
        } else {
            throw new UnsupportedOperationException(String.format("writeMessage unsupported magic, magic: %s", message.getMagic()));
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

    public static KafkaBrokerMessage readMessage(ByteBuffer buffer) throws Exception {
        byte magic = buffer.get(MAGIC_OFFSET);
        if (magic == MESSAGE_MAGIC_V0) {
            return KafkaMessageV0Serializer.readMessage(buffer);
        } else if (magic == MESSAGE_MAGIC_V1) {
            return KafkaMessageV1Serializer.readMessage(buffer);
        } else {
            throw new UnsupportedOperationException(String.format("readMessage unsupported magic, magic: %s", magic));
        }
    }

}