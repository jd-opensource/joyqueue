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
package io.openmessaging.joyqueue.extension;

import com.google.common.collect.Lists;
import org.joyqueue.client.internal.metadata.domain.PartitionMetadata;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import io.openmessaging.extension.QueueMetaData;

import java.util.List;

/**
 * QueueMetaDataAdapter
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
public class QueueMetaDataAdapter implements QueueMetaData {

    private TopicMetadata topicMetadata;

    public QueueMetaDataAdapter(TopicMetadata topicMetadata) {
        this.topicMetadata = topicMetadata;
    }

    @Override
    public String queueName() {
        return topicMetadata.getTopic();
    }

    @Override
    public List<Partition> partitions() {
        List<PartitionMetadata> partitions = topicMetadata.getPartitions();
        List<Partition> result = Lists.newArrayListWithCapacity(partitions.size());
        for (PartitionMetadata partition : partitions) {
            result.add(new PartitionAdapter(partition));
        }
        return result;
    }

    @Override
    public int hashCode() {
        return topicMetadata.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof QueueMetaDataAdapter)) {
            return false;
        }
        return topicMetadata.equals(((QueueMetaDataAdapter) obj).getTopicMetadata());
    }

    @Override
    public String toString() {
        return topicMetadata.toString();
    }

    public TopicMetadata getTopicMetadata() {
        return topicMetadata;
    }
}