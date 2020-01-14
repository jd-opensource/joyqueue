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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.joyqueue.client.internal.Plugins;
import org.joyqueue.client.internal.producer.PartitionSelector;

import java.util.concurrent.ConcurrentMap;

/**
 * PartitionSelectorManager
 *
 * author: gaohaoxiang
 * date: 2019/1/3
 */
public class PartitionSelectorManager {

    private ConcurrentMap<String, PartitionSelector> partitionSelectorMap = Maps.newConcurrentMap();

    public PartitionSelector getPartitionSelector(String topic, String selectorType) {
        PartitionSelector partitionSelector = partitionSelectorMap.get(topic);
        if (partitionSelector == null) {
            partitionSelector = create(selectorType);
            PartitionSelector oldPartitionSelector = partitionSelectorMap.putIfAbsent(topic, partitionSelector);
            if (oldPartitionSelector != null) {
                partitionSelector = oldPartitionSelector;
            }
        }
        return partitionSelector;
    }

    private PartitionSelector create(String selectorType) {
        PartitionSelector selector = Plugins.PARTITION_SELECTOR.get(selectorType);
        Preconditions.checkArgument(selector != null, String.format("partition selector not found. type: %s", selectorType));
        return selector;
    }
}