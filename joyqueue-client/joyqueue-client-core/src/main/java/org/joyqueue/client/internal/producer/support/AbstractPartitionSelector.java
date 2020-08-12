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
package org.joyqueue.client.internal.producer.support;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.joyqueue.client.internal.metadata.domain.PartitionMetadata;
import org.joyqueue.client.internal.metadata.domain.PartitionNode;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.client.internal.producer.PartitionSelector;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.network.domain.BrokerNode;

import java.util.List;

/**
 * AbstractPartitionSelector
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public abstract class AbstractPartitionSelector implements PartitionSelector {

    @Override
    public PartitionNode select(ProduceMessage message, TopicMetadata topicMetadata, List<BrokerNode> brokerNodes) {
        if (CollectionUtils.isEmpty(brokerNodes)) {
            return null;
        }
        if (message.getPartition() != ProduceMessage.NONE_PARTITION) {
            return selectPartition(message, topicMetadata, brokerNodes);
        }
        if (message.getPartitionKey() != ProduceMessage.NONE_PARTITION_KEY) {
            return hashPartition(message, topicMetadata, brokerNodes);
        }
        if (brokerNodes.size() == 1) {
            return randomSelectPartitionNode(topicMetadata, brokerNodes.get(0));
        }
        return nextPartition(message, topicMetadata, brokerNodes);
    }

    protected PartitionNode selectPartition(ProduceMessage message, TopicMetadata topicMetadata, List<BrokerNode> brokerNodes) {
        PartitionMetadata partitionMetadata = topicMetadata.getPartition(message.getPartition());
        if (partitionMetadata == null) {
            return null;
        }
        if (!brokerNodes.contains(partitionMetadata.getLeader())) {
            return null;
        }
        return new PartitionNode(partitionMetadata);
    }

    protected PartitionNode hashPartition(ProduceMessage message, TopicMetadata topicMetadata, List<BrokerNode> brokerNodes) {
        int hashCode = message.getPartitionKey().hashCode();
        BrokerNode brokerNode = brokerNodes.get(Math.abs(hashCode % brokerNodes.size()));
        List<PartitionMetadata> partitions = topicMetadata.getBrokerPartitions(brokerNode.getId());
        return new PartitionNode(partitions.get(Math.abs(hashCode % partitions.size())));
    }

    protected PartitionNode randomSelectPartitionNode(TopicMetadata topicMetadata, BrokerNode brokerNode) {
        return new PartitionNode(randomSelectPartition(topicMetadata, brokerNode));
    }

    protected PartitionMetadata randomSelectPartition(TopicMetadata topicMetadata, BrokerNode brokerNode) {
        List<PartitionMetadata> brokerPartitions = topicMetadata.getBrokerPartitions(brokerNode.getId());
        return brokerPartitions.get(RandomUtils.nextInt(0, brokerPartitions.size()));
    }

    protected abstract PartitionNode nextPartition(ProduceMessage message, TopicMetadata topicMetadata, List<BrokerNode> brokerNodes);
}