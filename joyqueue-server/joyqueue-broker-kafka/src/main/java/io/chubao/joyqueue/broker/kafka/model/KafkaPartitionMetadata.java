package io.chubao.joyqueue.broker.kafka.model;

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