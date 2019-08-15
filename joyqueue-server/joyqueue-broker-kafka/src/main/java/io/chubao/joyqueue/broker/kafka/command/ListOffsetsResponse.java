package io.chubao.joyqueue.broker.kafka.command;

import io.chubao.joyqueue.broker.kafka.KafkaCommandType;

import java.util.List;
import java.util.Map;

/**
 * ListOffsetsResponse
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class ListOffsetsResponse extends KafkaRequestOrResponse {

    private Map<String, List<PartitionOffsetResponse>> partitionResponses;

    public ListOffsetsResponse() {

    }

    public ListOffsetsResponse(Map<String, List<PartitionOffsetResponse>> partitionResponses) {
        this.partitionResponses = partitionResponses;
    }

    public void setPartitionResponses(Map<String, List<PartitionOffsetResponse>> partitionResponses) {
        this.partitionResponses = partitionResponses;
    }

    public Map<String, List<PartitionOffsetResponse>> getPartitionResponses() {
        return partitionResponses;
    }

    @Override
    public int type() {
        return KafkaCommandType.LIST_OFFSETS.getCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Name: " + this.getClass().getSimpleName());
        return builder.toString();
    }

    public static class PartitionOffsetResponse {

        private int partition;
        private short errorCode;
        private long timestamp;
        private long offset;

        public PartitionOffsetResponse(int partition, short errorCode, long timestamp, long offset) {
            this.partition = partition;
            this.errorCode = errorCode;
            this.offset = offset;
            this.timestamp = timestamp;
        }

        public void setPartition(int partition) {
            this.partition = partition;
        }

        public int getPartition() {
            return partition;
        }

        public short getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(short errorCode) {
            this.errorCode = errorCode;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public long getOffset() {
            return offset;
        }

        public void setOffset(long offset) {
            this.offset = offset;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Name: " + this.getClass().getSimpleName());
            stringBuilder.append(",errorCode : " + errorCode);
            stringBuilder.append(",offset :" + offset);
            return stringBuilder.toString();
        }
    }
}
