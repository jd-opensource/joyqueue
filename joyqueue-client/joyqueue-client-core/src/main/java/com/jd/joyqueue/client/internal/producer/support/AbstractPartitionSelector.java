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
package com.jd.joyqueue.client.internal.producer.support;

import com.jd.joyqueue.client.internal.metadata.domain.PartitionMetadata;
import com.jd.joyqueue.client.internal.metadata.domain.TopicMetadata;
import com.jd.joyqueue.client.internal.producer.PartitionSelector;
import com.jd.joyqueue.client.internal.producer.domain.ProduceMessage;

import java.util.List;

/**
 * AbstractPartitionSelector
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public abstract class AbstractPartitionSelector implements PartitionSelector {

    @Override
    public PartitionMetadata select(ProduceMessage message, TopicMetadata topicMetadata, List<PartitionMetadata> partitions) {
        if (message.getPartition() != ProduceMessage.NONE_PARTITION) {
            return selectPartition(message, topicMetadata, partitions);
        }
        if (message.getPartitionKey() != ProduceMessage.NONE_PARTITION_KEY) {
            return hashPartition(message, topicMetadata, partitions);
        }
        return nextPartition(message, topicMetadata, partitions);
    }

    protected PartitionMetadata selectPartition(ProduceMessage message, TopicMetadata topicMetadata, List<PartitionMetadata> partitions) {
        for (PartitionMetadata partition : partitions) {
            if (message.getPartition() == partition.getId()) {
                return partition;
            }
        }
        return null;
    }

    protected PartitionMetadata hashPartition(ProduceMessage message, TopicMetadata topicMetadata, List<PartitionMetadata> partitions) {
        return partitions.get(Math.abs(message.getPartitionKey().hashCode() % partitions.size()));
    }

    protected abstract PartitionMetadata nextPartition(ProduceMessage message, TopicMetadata topicMetadata, List<PartitionMetadata> partitions);
}