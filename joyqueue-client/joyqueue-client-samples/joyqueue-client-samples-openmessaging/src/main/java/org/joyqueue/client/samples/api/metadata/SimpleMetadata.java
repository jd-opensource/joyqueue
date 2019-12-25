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
package org.joyqueue.client.samples.api.metadata;

import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.extension.QueueMetaData;
import io.openmessaging.message.Message;
import io.openmessaging.producer.Producer;
import io.openmessaging.producer.SendResult;

/**
 * SimpleMetadata
 *
 * author: gaohaoxiang
 * date: 2019/4/8
 */
public class SimpleMetadata {

    public static void main(String[] args) {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint("oms:joyqueue://test_app@127.0.0.1:50088/UNKNOWN", keyValue);

        Producer producer = messagingAccessPoint.createProducer();
        producer.start();

        QueueMetaData queueMetaData = producer.getQueueMetaData("test_topic_0");
        for (QueueMetaData.Partition partition : queueMetaData.partitions()) {
            System.out.println(String.format("partition: %s, partitionHost: %s", partition.partitionId(), partition.partitonHost()));
        }

        Message message = producer.createMessage("test_topic_0", "body".getBytes());

        SendResult sendResult = producer.send(message);
        System.out.println(String.format("messageId: %s", sendResult.messageId()));
    }
}