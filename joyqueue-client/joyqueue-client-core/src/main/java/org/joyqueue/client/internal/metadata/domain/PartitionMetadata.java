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
import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * PartitionMetadata
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class PartitionMetadata implements Serializable {

    private short id;
    private int partitionGroupId;
    private String topic;
    private BrokerNode leader;

    public PartitionMetadata(short id, int partitionGroupId, String topic, BrokerNode leader) {
        this.id = id;
        this.partitionGroupId = partitionGroupId;
        this.topic = topic;
        this.leader = leader;
    }

    public short getId() {
        return id;
    }

    public int getPartitionGroupId() {
        return partitionGroupId;
    }

    public String getTopic() {
        return topic;
    }

    public BrokerNode getLeader() {
        return leader;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartitionMetadata that = (PartitionMetadata) o;
        return id == that.id &&
                partitionGroupId == that.partitionGroupId &&
                Objects.equal(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, partitionGroupId, topic);
    }

    @Override
    public String toString() {
        return "PartitionMetadata{" +
                "id=" + id +
                ", partitionGroupId=" + partitionGroupId +
                ", leader=" + leader +
                '}';
    }
}