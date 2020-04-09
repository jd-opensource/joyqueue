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

    void decreaseRetryProbability(Consumer consumer);

    List<Short> getPriorityPartition(TopicName topic);

    int getGroupByPartition(TopicName topic, short partition);

    boolean hasFreePartition(Consumer consumer);

    default void close() {}
}
