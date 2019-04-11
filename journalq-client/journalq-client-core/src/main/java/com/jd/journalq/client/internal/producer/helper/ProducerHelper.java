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
package com.jd.journalq.client.internal.producer.helper;

import com.google.common.collect.Lists;
import com.jd.journalq.client.internal.metadata.domain.PartitionMetadata;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.client.internal.producer.PartitionSelector;
import com.jd.journalq.client.internal.producer.domain.ProduceMessage;

import java.util.List;

/**
 * ProducerHelper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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

    public static PartitionMetadata dispatchPartitions(List<ProduceMessage> messages, TopicMetadata topicMetadata, List<PartitionMetadata> partitions, PartitionSelector partitionSelector) {
        PartitionMetadata partition = partitionSelector.select(messages.get(0), topicMetadata, partitions);
        if (partition == null || partition.getLeader() == null) {
            return null;
        }
        return partition;
    }

    public static List<PartitionMetadata> filterBlackList(List<PartitionMetadata> partitions, List<PartitionMetadata> partitionBlackList) {
        List<PartitionMetadata> newPartitions = Lists.newArrayList(partitions);
        for (PartitionMetadata blackPartitionMetadata : partitionBlackList) {
            newPartitions.remove(blackPartitionMetadata);
        }
        return newPartitions;
    }

}