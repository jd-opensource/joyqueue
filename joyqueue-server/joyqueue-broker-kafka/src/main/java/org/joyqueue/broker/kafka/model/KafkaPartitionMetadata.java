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
package org.joyqueue.broker.kafka.model;

import java.util.List;

/**
 * Created by zhangkepeng on 16-8-2.
 */
public class KafkaPartitionMetadata {

    private int partition;
    private KafkaBroker leader;
    private List<KafkaBroker> replicas;
    private List<KafkaBroker> isrs;
    private short errorCode;

    public KafkaPartitionMetadata(int partition, KafkaBroker leader, List<KafkaBroker> replicas, List<KafkaBroker> isrs, short errorCode) {
        this.partition = partition;
        this.replicas = replicas;
        this.isrs = isrs;
        this.leader = leader;
        this.errorCode = errorCode;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public KafkaBroker getLeader() {
        return leader;
    }

    public void setLeader(KafkaBroker leader) {
        this.leader = leader;
    }

    public List<KafkaBroker> getReplicas() {
        return replicas;
    }

    public void setReplicas(List<KafkaBroker> replicas) {
        this.replicas = replicas;
    }

    public List<KafkaBroker> getIsr() {
        return isrs;
    }

    public void setIsr(List<KafkaBroker> isrs) {
        this.isrs = isrs;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "KafkaPartitionMetadata{" +
                "partition=" + partition +
                ", leader=" + leader +
                ", replicas=" + replicas +
                ", isrs=" + isrs +
                ", errorCode=" + errorCode +
                '}';
    }
}