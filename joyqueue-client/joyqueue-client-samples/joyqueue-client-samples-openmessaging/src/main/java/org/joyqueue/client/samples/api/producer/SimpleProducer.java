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
package org.joyqueue.client.samples.api.producer;

import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.message.Message;
import io.openmessaging.producer.Producer;
import io.openmessaging.producer.SendResult;

/**
 * SimpleProducer
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public class SimpleProducer {

    public static void main(String[] args) {

        final String app = "test_app";
        final String token = "some token";
        final String dataCenter = "default";
        final String brokerAddr = "127.0.0.1:50088";
        final String topic = "test_topic_0";
        // oms:joyqueue://test_app@127.0.0.1:50088/default
        final String url = "oms:joyqueue://" + app +  "@" + brokerAddr + "/" + dataCenter;

        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, token);

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(url, keyValue);

        // 使用MessagingAccessPoint创建producer
        Producer producer = messagingAccessPoint.createProducer();
        producer.start();

        // 使用producer.createMessage方法创建message
        Message message = producer.createMessage(topic, "Message body".getBytes());

        // 设置messageKey，非必填
        // 如果需要相对顺序消息，也可以使用messageKey作为key指定分区
//        message.extensionHeader().get().setMessageKey("test_key");

        // 自定义发送的partition
        // 最好根据元数据自定义分配，不要写死partitions
//        List<QueueMetaData.Partition> partitions = producer.getQueueMetaData("test_topic_0").partitions();
//        QueueMetaData.Partition partition = partitions.get((int) SystemClock.now() % partitions.size());
//        message.extensionHeader().get().setPartition(partition.partitionId());

        // 生产消息，不抛异常就算成功，sendResult里的messageId暂时没有意义
        SendResult sendResult = producer.send(message);

        // 打印生产结果
        System.out.println(String.format("messageId: %s", sendResult.messageId()));
    }
}