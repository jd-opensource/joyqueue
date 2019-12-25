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
package org.joyqueue.client.internal.metadata.domain;

import org.joyqueue.network.domain.BrokerNode;

import java.io.Serializable;
import java.util.Map;

/**
 * PartitionGroupMetadata
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class PartitionGroupMetadata implements Serializable {

    private int id;
    private BrokerNode leader;
    private Map<Short, PartitionMetadata> partitions;

    public PartitionGroupMetadata(int id, BrokerNode leader, Map<Short, PartitionMetadata> partitions) {
        this.id = id;
        this.leader = leader;
        this.partitions = partitions;
    }

    public int getId() {
        return id;
    }

    public BrokerNode getLeader() {
        return leader;
    }

    public Map<Short, PartitionMetadata> getPartitions() {
        return partitions;
    }

    @Override
    public String toString() {
        return "PartitionGroupMetadata{" +
                "id=" + id +
                ", leader=" + leader +
                ", partitions=" + partitions +
                '}';
    }
}