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
import org.joyqueue.broker.network.traffic.ProduceResponseTrafficPayload;
import org.joyqueue.broker.network.traffic.Traffic;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangkepeng on 16-8-1.
 */
public class ProduceResponse extends KafkaRequestOrResponse implements ProduceResponseTrafficPayload {

    private Traffic traffic;
    private Map<String, List<PartitionResponse>> partitionResponses;

    public ProduceResponse() {

    }

    public ProduceResponse(Traffic traffic, Map<String, List<PartitionResponse>> partitionResponses) {
        this.traffic = traffic;
        this.partitionResponses = partitionResponses;
    }

    public void setPartitionResponses(Map<String, List<PartitionResponse>> partitionResponses) {
        this.partitionResponses = partitionResponses;
    }

    public Map<String, List<PartitionResponse>> getPartitionResponses() {
        return partitionResponses;
    }

    @Override
    public Traffic getTraffic() {
        return traffic;
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

        public PartitionResponse(long offset, short errorCode) {
            this.offset = offset;
            this.errorCode = errorCode;
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