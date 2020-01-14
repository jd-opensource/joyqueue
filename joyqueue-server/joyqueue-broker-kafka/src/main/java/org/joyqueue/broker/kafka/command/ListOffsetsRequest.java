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
package org.joyqueue.broker.kafka.command;

import org.joyqueue.broker.kafka.KafkaCommandType;

import java.util.List;
import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/10.
 */
public class ListOffsetsRequest extends KafkaRequestOrResponse {

    private int replicaId;
    private byte isolationLevel;
    private Map<String, List<PartitionOffsetRequest>> partitionRequests;

    public void setIsolationLevel(byte isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    public byte getIsolationLevel() {
        return isolationLevel;
    }

    public int getReplicaId() {
        return replicaId;
    }

    public void setReplicaId(int replicaId) {
        this.replicaId = replicaId;
    }

    public void setPartitionRequests(Map<String, List<PartitionOffsetRequest>> partitionRequests) {
        this.partitionRequests = partitionRequests;
    }

    public Map<String, List<PartitionOffsetRequest>> getPartitionRequests() {
        return partitionRequests;
    }

    @Override
    public int type() {
        return KafkaCommandType.LIST_OFFSETS.getCode();
    }

    @Override
    public String toString() {
        return describe();
    }

    public String describe() {
        StringBuilder offsetRequest = new StringBuilder();
        offsetRequest.append("Name: " + this.getClass().getSimpleName());
        offsetRequest.append("; ReplicaId: " + replicaId);
        return offsetRequest.toString();
    }

    public static class PartitionOffsetRequest {

        private int partition;
        private long time;
        private int maxNumOffsets;

        public PartitionOffsetRequest() {

        }

        public PartitionOffsetRequest(int partition, long time, int maxNumOffsets) {
            this.partition = partition;
            this.time = time;
            this.maxNumOffsets = maxNumOffsets;
        }

        public void setPartition(int partition) {
            this.partition = partition;
        }

        public int getPartition() {
            return partition;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public long getTime() {
            return time;
        }

        public void setMaxNumOffsets(int maxNumOffsets) {
            this.maxNumOffsets = maxNumOffsets;
        }

        public int getMaxNumOffsets() {
            return maxNumOffsets;
        }
    }
}
