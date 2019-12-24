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
package org.joyqueue.client.internal.consumer.coordinator.domain;

import org.joyqueue.network.domain.BrokerNode;

/**
 * BrokerAssignment
 *
 * author: gaohaoxiang
 * date: 2018/12/11
 */
public class BrokerAssignment {

    private BrokerNode broker;
    private PartitionAssignment partitionAssignment;

    public BrokerAssignment() {

    }

    public BrokerAssignment(BrokerNode broker, PartitionAssignment partitionAssignment) {
        this.broker = broker;
        this.partitionAssignment = partitionAssignment;
    }

    public BrokerNode getBroker() {
        return broker;
    }

    public void setBroker(BrokerNode broker) {
        this.broker = broker;
    }

    public PartitionAssignment getPartitionAssignment() {
        return partitionAssignment;
    }

    public void setPartitionAssignment(PartitionAssignment partitionAssignment) {
        this.partitionAssignment = partitionAssignment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BrokerAssignment that = (BrokerAssignment) o;
        return that.getBroker().equals(broker);
    }

    @Override
    public int hashCode() {
        return broker.hashCode();
    }

    @Override
    public String toString() {
        return "BrokerAssignment{" +
                "broker=" + broker +
                ", partitionAssignment=" + partitionAssignment +
                '}';
    }
}