package io.chubao.joyqueue.domain;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2018/9/7
 */
public class Partition implements Serializable {
    /**
     * retry partition ID
     */

    public static final short RETRY_PARTITION_ID = Short.MAX_VALUE;
    /**
     * max partition id
     */
    public static final short MAX_PARTITION_ID = Short.MAX_VALUE - 1;
    /**
     * min partition id
     */
    public static final short MIN_PARTITION_ID = 0;
    /**
     * partition ID
     */
    private short partitionId;
    /**
     * leader broker
     */
    private Broker leader;
    /**
     * partition replica
     */
    private Set<Broker> replicas;
    /**
     * in sync replica
     */
    private Set<Broker> isrs;

    public Partition(short partitionId) {
        this(partitionId, null, null, null);
    }

    public Partition(short partitionId, Broker leader, Set<Broker> replicas, Set<Broker> isrs) {
        Preconditions.checkArgument(partitionId >= MIN_PARTITION_ID && partitionId <= MAX_PARTITION_ID, "partition id must in [0,32767]");
        this.partitionId = partitionId;
        this.leader = leader;
        this.replicas = replicas;
        this.isrs = isrs;
    }

    public short getPartitionId() {
        return partitionId;
    }

    public Broker getLeader() {
        return leader;
    }

    public Set<Broker> getReplicas() {
        return replicas;
    }

    public Set<Broker> getIsrs() {
        return isrs;
    }
}
