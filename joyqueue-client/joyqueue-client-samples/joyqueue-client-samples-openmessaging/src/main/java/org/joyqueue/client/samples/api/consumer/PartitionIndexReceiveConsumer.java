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
package org.joyqueue.client.samples.api.consumer;

import com.google.common.collect.Maps;
import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.extension.QueueMetaData;
import io.openmessaging.joyqueue.consumer.ExtensionConsumer;
import io.openmessaging.message.Message;

import java.util.Map;

/**
 * PartitionIndexReceiveConsumer
 *
 * author: gaohaoxiang
 * date: 2019/2/20
 */
public class PartitionIndexReceiveConsumer {

    public static void main(String[] args) throws Exception {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint("oms:joyqueue://test_app@127.0.0.1:50088/UNKNOWN", keyValue);

        // 首先需要对consumer强制转型
        ExtensionConsumer extensionConsumer = (ExtensionConsumer) messagingAccessPoint.createConsumer();
        extensionConsumer.start();

        // 绑定主题，将要消费的主题
        extensionConsumer.bindQueue("test_topic_0");

        // 获取元数据
        QueueMetaData queueMetaData = extensionConsumer.getQueueMetaData("test_topic_0");
        Map<Integer, Long> indexMapper = Maps.newHashMap();

        // 这里只是简单例子，根据具体情况进行调度处理
        while (true) {
            // 循环所有partition，并拉取相应消息
            for (QueueMetaData.Partition partition : queueMetaData.partitions()) {
                // 初始化index信息
                Long index = indexMapper.get(partition.partitionId());
                if (index == null) {
                    index = 0L;
                    indexMapper.put(partition.partitionId(), index);
                }

                System.out.println("doReceive");

                // 拉取分区单条消息
                // 参数是超时时间，只是网络请求的超时
                Message message = extensionConsumer.receive((short) partition.partitionId(), index, 1000 * 10);
                // 批量拉取的方式相同，不单独列出
//                List<Message> messages = extensionConsumer.batchReceive((short) partition.partitionId(), index, 1000 * 10);

                // 没有拉取到，继续循环
                if (message == null) {
                    continue;
                }

                // 拉取到消息，打印日志并ack
                System.out.println(String.format("receive, message: %s", message));
                extensionConsumer.ack(message.getMessageReceipt());

                // 更新index到下一个消息处
                index = message.extensionHeader().get().getOffset() + 1;
                indexMapper.put(partition.partitionId(), index);
            }

            Thread.currentThread().sleep(1000 * 1);
        }
    }
}
