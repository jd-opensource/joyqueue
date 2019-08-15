package io.chubao.joyqueue.client.internal.consumer.coordinator.domain;

import io.chubao.joyqueue.network.domain.BrokerNode;

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