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
