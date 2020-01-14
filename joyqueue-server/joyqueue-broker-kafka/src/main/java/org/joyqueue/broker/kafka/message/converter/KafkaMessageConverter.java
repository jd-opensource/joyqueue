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
package org.joyqueue.broker.kafka.message.converter;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.joyqueue.broker.kafka.message.KafkaBrokerMessage;
import org.joyqueue.broker.kafka.message.KafkaMessageSerializer;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.message.SourceType;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.time.SystemClock;

import java.net.InetSocketAddress;
import java.util.List;


/**
 * kafka消息和broker消息的转换
 *
 * author: gaohaoxiang
 * date: 2018/8/28
 */
public class KafkaMessageConverter {

    public static List<KafkaBrokerMessage> toKafkaBrokerMessage(String topic, int partition, List<BrokerMessage> brokerMessages) {
        List<KafkaBrokerMessage> result = Lists.newLinkedList();
        for (BrokerMessage message : brokerMessages) {
            KafkaBrokerMessage kafkaBrokerMessage = toKafkaBrokerMessage(topic, partition, message);
            result.add(kafkaBrokerMessage);
        }
        return result;
    }

    public static KafkaBrokerMessage toKafkaBrokerMessage(String topic, int partition, BrokerMessage brokerMessage) {
        KafkaBrokerMessage kafkaBrokerMessage = new KafkaBrokerMessage();
        kafkaBrokerMessage.setOffset(brokerMessage.getMsgIndexNo());
        kafkaBrokerMessage.setKey(brokerMessage.getBusinessId() == null ? null : brokerMessage.getBusinessId().getBytes(Charsets.UTF_8));
        kafkaBrokerMessage.setValue(brokerMessage.getByteBody());
        kafkaBrokerMessage.setBatch(brokerMessage.isBatch());
        kafkaBrokerMessage.setFlag(brokerMessage.getFlag());
        KafkaMessageSerializer.readExtension(brokerMessage, kafkaBrokerMessage);
        return kafkaBrokerMessage;
    }

    public static List<BrokerMessage> toBrokerMessages(String topic, int partition, String clientId, InetSocketAddress clientAddress, List<KafkaBrokerMessage> kafkaBrokerMessages) {
        List<BrokerMessage> result = Lists.newLinkedList();
        byte[] clientAddressBytes = IpUtil.toByte(clientAddress);
        for (KafkaBrokerMessage message : kafkaBrokerMessages) {
            BrokerMessage brokerMessage = toBrokerMessage(topic, partition, clientId, clientAddressBytes, message);
            result.add(brokerMessage);
        }
        return result;
    }

    public static BrokerMessage toBrokerMessage(String topic, int partition, String clientId, InetSocketAddress clientAddress, KafkaBrokerMessage kafkaBrokerMessage) {
        return toBrokerMessage(topic, partition, clientId, IpUtil.toByte(clientAddress), kafkaBrokerMessage);
    }

    public static BrokerMessage toBrokerMessage(String topic, int partition, String clientId, byte[] clientAddress, KafkaBrokerMessage kafkaBrokerMessage) {
        BrokerMessage brokerMessage = new BrokerMessage();
        brokerMessage.setTopic(topic);
        brokerMessage.setApp(clientId);
        brokerMessage.setPartition((short) partition);
        brokerMessage.setCompressed(false);
        brokerMessage.setClientIp(clientAddress);
        brokerMessage.setBusinessId(kafkaBrokerMessage.getKey() == null ? null : new String(kafkaBrokerMessage.getKey(), Charsets.UTF_8));
        brokerMessage.setBody(kafkaBrokerMessage.getValue());
        brokerMessage.setStartTime(SystemClock.now());
        brokerMessage.setSource(SourceType.KAFKA.getValue());
        brokerMessage.setBatch(kafkaBrokerMessage.isBatch());
        brokerMessage.setFlag(kafkaBrokerMessage.getFlag());
        KafkaMessageSerializer.writeExtension(brokerMessage, kafkaBrokerMessage);

        return brokerMessage;
    }
}
