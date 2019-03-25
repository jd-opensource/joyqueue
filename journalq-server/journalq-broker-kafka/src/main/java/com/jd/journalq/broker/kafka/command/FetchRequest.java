package com.jd.journalq.broker.kafka.command;


import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.model.IsolationLevel;
import com.jd.journalq.domain.TopicName;

/**
 * Created by zhangkepeng on 16-7-27.
 */
public class FetchRequest extends KafkaRequestOrResponse {
    private int replicaId;
    private int maxWait;
    private int minBytes;
    private int maxBytes;
    private IsolationLevel isolationLevel;
    private Table<TopicName, Integer, PartitionFetchInfo> requestInfo;
    private int numPartitions;

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

    public IsolationLevel getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(IsolationLevel isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    public Table<TopicName, Integer, PartitionFetchInfo> getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(Table<TopicName, Integer, PartitionFetchInfo> requestInfo) {
        this.requestInfo = requestInfo;
    }

    public void setNumPartitions(int numPartitions) {
        this.numPartitions = numPartitions;
    }

    public int getNumPartitions() {
        return numPartitions;
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

    public class PartitionFetchInfo {

        private long offset;
        private long logStartOffset;
        private int maxBytes;

        public PartitionFetchInfo(long offset, int maxBytes) {
            this.offset = offset;
            this.maxBytes = maxBytes;
        }

        public PartitionFetchInfo(long offset, int maxBytes, long logStartOffset) {
            this.offset = offset;
            this.maxBytes = maxBytes;
            this.logStartOffset = logStartOffset;
        }

        public long getOffset() {
            return offset;
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
