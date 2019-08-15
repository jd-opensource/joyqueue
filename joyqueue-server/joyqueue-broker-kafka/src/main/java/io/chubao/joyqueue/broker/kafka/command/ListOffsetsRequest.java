package io.chubao.joyqueue.broker.kafka.command;

import io.chubao.joyqueue.broker.kafka.KafkaCommandType;

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
