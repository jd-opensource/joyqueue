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
package org.joyqueue.client.internal.producer.helper;

import org.joyqueue.client.internal.metadata.domain.PartitionNode;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.client.internal.producer.PartitionSelector;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.network.domain.BrokerNode;

import java.util.List;

/**
 * ProducerHelper
 *
 * author: gaohaoxiang
 * date: 2018/12/26
 */
public class ProducerHelper {

    public static void setPartitions(List<ProduceMessage> messages, short partition) {
        for (ProduceMessage message : messages) {
            message.setPartition(partition);
        }
    }

    public static void clearPartitions(List<ProduceMessage> messages) {
        for (ProduceMessage message : messages) {
            clearPartition(message);
        }
    }

    public static void clearPartition(ProduceMessage message) {
        message.setPartition(ProduceMessage.NONE_PARTITION);
        message.setPartitionKey(ProduceMessage.NONE_PARTITION_KEY);
    }

    public static PartitionNode dispatchPartitions(List<ProduceMessage> messages, TopicMetadata topicMetadata, List<BrokerNode> brokerNodes, PartitionSelector partitionSelector) {
        return partitionSelector.select(messages.get(0), topicMetadata, brokerNodes);
    }
}