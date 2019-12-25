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
import org.joyqueue.broker.kafka.model.OffsetAndMetadata;

import java.util.List;
import java.util.Map;

/**
 * TxnOffsetCommitRequest
 *
 * author: gaohaoxiang
 * date: 2019/4/4
 */
public class TxnOffsetCommitRequest extends KafkaRequestOrResponse {

    private String transactionId;
    private String groupId;
    private long producerId;
    private short producerEpoch;
    private Map<String, List<OffsetAndMetadata>> partitions;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public long getProducerId() {
        return producerId;
    }

    public void setProducerId(long producerId) {
        this.producerId = producerId;
    }

    public short getProducerEpoch() {
        return producerEpoch;
    }

    public void setProducerEpoch(short producerEpoch) {
        this.producerEpoch = producerEpoch;
    }

    public void setPartitions(Map<String, List<OffsetAndMetadata>> partitions) {
        this.partitions = partitions;
    }

    public Map<String, List<OffsetAndMetadata>> getPartitions() {
        return partitions;
    }

    @Override
    public int type() {
        return KafkaCommandType.TXN_OFFSET_COMMIT.getCode();
    }

    @Override
    public String toString() {
        return "TxnOffsetCommitRequest{" +
                "transactionId='" + transactionId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", producerId=" + producerId +
                ", producerEpoch=" + producerEpoch +
                ", partitions=" + partitions +
                '}';
    }
}
