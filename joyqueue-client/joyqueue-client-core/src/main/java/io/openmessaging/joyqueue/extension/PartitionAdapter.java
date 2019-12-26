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

import org.joyqueue.client.internal.metadata.domain.PartitionMetadata;
import org.joyqueue.network.domain.BrokerNode;
import io.openmessaging.extension.QueueMetaData;

/**
 * PartitionAdapter
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
public class PartitionAdapter implements QueueMetaData.Partition {

    private PartitionMetadata partitionMetadata;

    public PartitionAdapter(PartitionMetadata partitionMetadata) {
        this.partitionMetadata = partitionMetadata;
    }

    @Override
    public int partitionId() {
        return partitionMetadata.getId();
    }

    @Override
    public String partitonHost() {
        BrokerNode leader = partitionMetadata.getLeader();
        if (leader == null) {
            return null;
        }
        return leader.getHost() + ":" + leader.getPort();
    }
}