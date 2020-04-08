package org.joyqueue.broker.consumer;

import org.joyqueue.broker.consumer.model.ConsumePartition;
import org.joyqueue.broker.consumer.model.OwnerShip;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.network.session.Consumer;

import java.util.List;

/**
 * @author LiYue
 * Date: 2020/4/8
 */
public interface PartitionManager {
    boolean tryOccupyPartition(Consumer consumer, short partition, long occupyTimeout);

    boolean releasePartition(Consumer consumer, short partition);

    boolean releasePartition(ConsumePartition consumePartition);

    boolean needPause(Consumer consumer) throws JoyQueueException;

    void increaseSerialErr(OwnerShip ownerShip);

    void clearSerialErr(Consumer consumer);

    int selectPartitionIndex(int partitionSize, int partitionIndex, long accessTimes);

    boolean isRetry(Consumer consumer) throws JoyQueueException;

    void resetRetryProbability(Integer maxProbability);

    void increaseRetryProbability(Consumer consumer);

    abstract void decreaseRetryProbability(Consumer consumer);

    List<Short> getPriorityPartition(TopicName topic);

    int getGroupByPartition(TopicName topic, short partition);

    boolean hasFreePartition(Consumer consumer);

    default void close() {}
}
