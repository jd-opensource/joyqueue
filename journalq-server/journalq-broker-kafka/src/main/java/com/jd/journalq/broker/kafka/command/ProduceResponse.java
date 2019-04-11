package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.KafkaCommandType;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangkepeng on 16-8-1.
 */
public class ProduceResponse extends KafkaRequestOrResponse {

    private Map<String, List<PartitionResponse>> partitionResponses;

    public ProduceResponse() {

    }

    public ProduceResponse(Map<String, List<PartitionResponse>> partitionResponses) {
        this.partitionResponses = partitionResponses;
    }

    public void setPartitionResponses(Map<String, List<PartitionResponse>> partitionResponses) {
        this.partitionResponses = partitionResponses;
    }

    public Map<String, List<PartitionResponse>> getPartitionResponses() {
        return partitionResponses;
    }

    @Override
    public int type() {
        return KafkaCommandType.PRODUCE.getCode();
    }

    public static class PartitionResponse {

        public static final short NONE_OFFSET = 0;

        private int partition;
        private short errorCode;
        private long offset;
        private long logAppendTime = -1L;
        private long logStartOffset = 0L;

        public PartitionResponse(short errorCode) {
            this.errorCode = errorCode;
        }

        public PartitionResponse(short errorCode, long offset) {
            this.errorCode = errorCode;
            this.offset = offset;
        }

        public PartitionResponse(int partition, long offset, short errorCode) {
            this.partition = partition;
            this.offset = offset;
            this.errorCode = errorCode;
        }

        public void setLogStartOffset(long logStartOffset) {
            this.logStartOffset = logStartOffset;
        }

        public long getLogStartOffset() {
            return logStartOffset;
        }

        public void setLogAppendTime(long logAppendTime) {
            this.logAppendTime = logAppendTime;
        }

        public long getLogAppendTime() {
            return logAppendTime;
        }

        public int getPartition() {
            return partition;
        }

        public void setPartition(int partition) {
            this.partition = partition;
        }

        public short getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(short errorCode) {
            this.errorCode = errorCode;
        }

        public long getOffset() {
            return offset;
        }

        public void setOffset(long offset) {
            this.offset = offset;
        }

    }
}