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
import org.joyqueue.broker.kafka.helper.KafkaClientHelper;
import org.joyqueue.broker.network.traffic.FetchRequestTrafficPayload;
import org.joyqueue.broker.network.traffic.Traffic;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangkepeng on 16-7-27.
 */
public class FetchRequest extends KafkaRequestOrResponse implements FetchRequestTrafficPayload {
    private int replicaId;
    private int maxWait;
    private int minBytes;
    private int maxBytes;
    private byte isolationLevel;
    private Map<String, List<PartitionRequest>> partitionRequests;
    private Traffic traffic = new Traffic();

    @Override
    public Traffic getTraffic() {
        return traffic;
    }

    public int getMinBytes() {
        return minBytes;
    }

    public void setMinBytes(int minBytes) {
        this.minBytes = minBytes;
    }

    public int getMaxBytes() {
        return maxBytes;
    }

    public void setMaxBytes(int maxBytes) {
        this.maxBytes = maxBytes;
    }

    public byte getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(byte isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    public void setPartitionRequests(Map<String, List<PartitionRequest>> partitionRequests) {
        this.partitionRequests = partitionRequests;

        for (Map.Entry<String, List<PartitionRequest>> entry : partitionRequests.entrySet()) {
            traffic.record(entry.getKey(), 1, 1);
        }
    }

    @Override
    public void setClientId(String clientId) {
        // TODO clientId处理
        super.setClientId(clientId);
        traffic.setApp(KafkaClientHelper.parseClient(clientId));
    }

    public Map<String, List<PartitionRequest>> getPartitionRequests() {
        return partitionRequests;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public int getReplicaId() {
        return replicaId;
    }

    public void setReplicaId(int replicaId) {
        this.replicaId = replicaId;
    }

    @Override
    public int type() {
        return KafkaCommandType.FETCH.getCode();
    }

    @Override
    public String toString() {
        return describe(true);
    }

    public String describe(boolean details) {
        StringBuilder fetchRequest = new StringBuilder();
        fetchRequest.append("Name: " + this.getClass().getSimpleName());
        fetchRequest.append("; ReplicaId: " + replicaId);
        fetchRequest.append("; MaxWait: " + maxWait + " ms");
        fetchRequest.append("; MinBytes: " + minBytes + " bytes");
        return fetchRequest.toString();
    }

    public static class PartitionRequest {

        private int partition;
        private long offset;
        private long logStartOffset;
        private int maxBytes;

        public void setPartition(int partition) {
            this.partition = partition;
        }

        public int getPartition() {
            return partition;
        }

        public void setOffset(long offset) {
            this.offset = offset;
        }

        public long getOffset() {
            return offset;
        }

        public void setMaxBytes(int maxBytes) {
            this.maxBytes = maxBytes;
        }

        public int getMaxBytes() {
            return maxBytes;
        }

        public long getLogStartOffset() {
            return logStartOffset;
        }

        public void setLogStartOffset(long logStartOffset) {
            this.logStartOffset = logStartOffset;
        }
    }

}
