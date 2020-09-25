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

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.RandomUtils;
import org.joyqueue.client.internal.metadata.domain.PartitionGroupMetadata;
import org.joyqueue.client.internal.metadata.domain.PartitionNode;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.network.domain.BrokerNode;

import java.util.List;

/**
 * WeightedRoundRobinPartitionSelector
 *
 * author: gaohaoxiang
 * date: 2018/12/27
 */
public class WeightedPartitionSelector extends AbstractPartitionSelector {

    public static final String NAME = "weighted";

    @Override
    protected PartitionNode nextPartition(ProduceMessage message, TopicMetadata topicMetadata, List<BrokerNode> brokerNodes) {
        double[] weights = new double[brokerNodes.size()];
        double weight = 0;
        int index = 0;

        if (topicMetadata.getProducerPolicy() != null && MapUtils.isNotEmpty(topicMetadata.getProducerPolicy().getWeight())) {
            for (BrokerNode brokerNode : brokerNodes) {
                weights[index] = brokerNode.getWeight();
                if (weights[index] < 0) {
                    weights[index] = 0;
                }
                weight += weights[index];
                index++;
            }
        } else {
            for (BrokerNode brokerNode : brokerNodes) {
                List<PartitionGroupMetadata> brokerPartitionGroups = topicMetadata.getBrokerPartitionGroups(brokerNode.getId());
                weights[index] = (brokerPartitionGroups != null ? brokerPartitionGroups.size() * 10 : brokerNode.getWeight());
                if (weights[index] < 0) {
                    weights[index] = 0;
                }
                weight += weights[index];
                index++;
            }
        }

        if (weight > 0) {
            int random = (int) (Math.random() * weight) + 1;
            weight = 0;
            for (int i = 0; i < weights.length; i++) {
                weight += weights[i];
                if (random <= weight) {
                    return randomSelectPartitionNode(topicMetadata, brokerNodes.get(i));
                }
            }
        }

        return randomSelectPartitionNode(topicMetadata, brokerNodes.get(RandomUtils.nextInt(0, brokerNodes.size())));
    }

    @Override
    public String type() {
        return NAME;
    }
}