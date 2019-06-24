/**
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
package com.jd.journalq.broker.kafka.command;


import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.message.KafkaBrokerMessage;
import com.jd.journalq.broker.network.traffic.FetchTrafficPayload;
import com.jd.journalq.broker.network.traffic.Traffic;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangkepeng on 16-8-4.
 */
public class FetchResponse extends KafkaRequestOrResponse implements FetchTrafficPayload {

    private Traffic traffic;
    private Map<String, List<PartitionResponse>> partitionResponses;

    public void setPartitionResponses(Map<String, List<PartitionResponse>> partitionResponses) {
        this.partitionResponses = partitionResponses;
    }

    public Map<String, List<PartitionResponse>> getPartitionResponses() {
        return partitionResponses;
    }

    public void setTraffic(Traffic traffic) {
        this.traffic = traffic;
    }

    @Override
    public int type() {
        return KafkaCommandType.FETCH.getCode();
    }

    @Override
    public Traffic getTraffic() {
        return traffic;
    }

    @Override
    public String toString() {
        StringBuilder responseStringBuilder = new StringBuilder();
        responseStringBuilder.append("Name: " + this.getClass().getSimpleName());
        responseStringBuilder.append("fetchResponses: " + partitionResponses);
        return responseStringBuilder.toString();
    }

    public static class PartitionResponse {

        private int partition;
        private short error = KafkaErrorCode.NONE.getCode();
        private long highWater = -1L;
        private long lastStableOffset = -1L;
        private long logStartOffset = -1L;
        private List<KafkaBrokerMessage> messages;
        private int bytes;

        public PartitionResponse() {

        }

        public PartitionResponse(int partition, short error) {
            this.partition = partition;
            this.error = error;
        }

        public PartitionResponse(int partition, short error, List<KafkaBrokerMessage> messages) {
            this.partition = partition;
            this.error = error;
            this.messages = messages;
        }

        public void setHighWater(long highWater) {
            this.highWater = highWater;
        }

        public long getHighWater() {
            return highWater;
        }

        public void setLogStartOffset(long logStartOffset) {
            this.logStartOffset = logStartOffset;
        }

        public long getLogStartOffset() {
            return logStartOffset;
        }

        public void setLastStableOffset(long lastStableOffset) {
            this.lastStableOffset = lastStableOffset;
        }

        public long getLastStableOffset() {
            return lastStableOffset;
        }

        public void setPartition(int partition) {
            this.partition = partition;
        }

        public int getPartition() {
            return partition;
        }

        public short getError() {
            return error;
        }

        public void setError(short error) {
            this.error = error;
        }

        public void setMessages(List<KafkaBrokerMessage> messages) {
            this.messages = messages;
        }

        public List<KafkaBrokerMessage> getMessages() {
            return messages;
        }

        public void setBytes(int bytes) {
            this.bytes = bytes;
        }

        public int getBytes() {
            return bytes;
        }
    }
}