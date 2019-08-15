package io.chubao.joyqueue.broker.protocol.coordinator.assignment;

/**
 * PartitionAssignorType
 *
 * author: gaohaoxiang
 * date: 2018/12/5
 */
@Deprecated
public enum PartitionAssignorType {

    /**
     * 空
     */
    NONE,

    /**
     * partitionGroup平衡
     */
    PARTITION_GROUP_BALANCE,

    /**
     * 独占
     */
    EXCLUSIVE

    ;
}