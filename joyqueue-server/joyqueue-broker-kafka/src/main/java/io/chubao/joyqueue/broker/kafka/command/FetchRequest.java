package io.chubao.joyqueue.broker.kafka.command;


import io.chubao.joyqueue.broker.kafka.KafkaCommandType;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangkepeng on 16-7-27.
 */
public class FetchRequest extends KafkaRequestOrResponse {
    private int replicaId;
    private int maxWait;
    private int minBytes;
    private int maxBytes;
    private byte isolationLevel;
    private Map<String, List<PartitionRequest>> partitionRequests;

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
