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
package org.joyqueue.broker.manage.converter;

import org.joyqueue.manage.IndexItem;
import org.joyqueue.manage.PartitionGroupMetric;
import org.joyqueue.manage.PartitionMetric;
import org.joyqueue.manage.TopicMetric;
import org.joyqueue.store.StoreManagementService;

import java.util.Arrays;

public class StoreManageConverter {

    public static TopicMetric[] convert(StoreManagementService.TopicMetric[] topicMetrics) {
        TopicMetric[] result = new TopicMetric[topicMetrics.length];
        for (int i = 0; i < topicMetrics.length; i++) {
            result[i] = convert(topicMetrics[i]);
        }
        return result;
    }

    public static TopicMetric convert(StoreManagementService.TopicMetric topicMetric) {
        TopicMetric result = new TopicMetric();
        result.setTopic(topicMetric.getTopic());
        result.setPartitionGroupMetrics(convert(topicMetric.getPartitionGroupMetrics()));
        return result;
    }

    public static PartitionGroupMetric[] convert(StoreManagementService.PartitionGroupMetric[] partitionGroupMetrics) {
        PartitionGroupMetric[] result = new PartitionGroupMetric[partitionGroupMetrics.length];
        for (int i = 0; i < partitionGroupMetrics.length; i++) {
            result[i] = convert(partitionGroupMetrics[i]);
        }
        return result;
    }

    public static PartitionGroupMetric convert(StoreManagementService.PartitionGroupMetric partitionGroupMetric) {
        PartitionGroupMetric result = new PartitionGroupMetric();
        result.setPartitionGroup(partitionGroupMetric.getPartitionGroup());
        result.setPartitionMetrics(convert(partitionGroupMetric.getPartitionMetrics()));
        result.setLeftPosition(partitionGroupMetric.getLeftPosition());
        result.setRightPosition(partitionGroupMetric.getRightPosition());
        result.setIndexPosition(partitionGroupMetric.getIndexPosition());
        result.setFlushPosition(partitionGroupMetric.getFlushPosition());
        result.setReplicationPosition(partitionGroupMetric.getReplicationPosition());
        return result;
    }

    public static PartitionMetric[] convert(StoreManagementService.PartitionMetric[] partitionMetrics) {
        PartitionMetric[] result = new PartitionMetric[partitionMetrics.length];
        for (int i = 0; i < partitionMetrics.length; i++) {
            result[i] = convert(partitionMetrics[i]);
        }
        return result;
    }

    public static PartitionMetric convert(StoreManagementService.PartitionMetric partitionMetric) {
        PartitionMetric result = new PartitionMetric();
        result.setPartition(partitionMetric.getPartition());
        result.setLeftIndex(partitionMetric.getLeftIndex());
        result.setRightIndex(partitionMetric.getRightIndex());
        return result;
    }

    public static IndexItem [] convert(Long [] indexItems) {

        return null == indexItems ? null : Arrays.stream(indexItems).map(i -> new IndexItem(i, 0)).toArray(IndexItem[]::new);
    }
}