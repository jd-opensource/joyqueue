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
package org.joyqueue.client.internal.consumer;

import com.google.common.collect.Table;
import org.joyqueue.client.internal.consumer.callback.BatchFetchListener;
import org.joyqueue.client.internal.consumer.callback.BatchPartitionFetchListener;
import org.joyqueue.client.internal.consumer.callback.FetchListener;
import org.joyqueue.client.internal.consumer.callback.PartitionFetchListener;
import org.joyqueue.client.internal.consumer.domain.FetchMessageData;
import org.joyqueue.network.domain.BrokerNode;
import org.joyqueue.toolkit.lang.LifeCycle;

import java.util.List;
import java.util.Map;

/**
 * MessageFetcher
 *
 * author: gaohaoxiang
 * date: 2018/12/7
 */
public interface MessageFetcher extends LifeCycle {

    FetchMessageData fetch(BrokerNode brokerNode, String topic, String app, int count, long timeout, long ackTimeout, long longPollTimeout);

    void asyncFetch(BrokerNode brokerNode, String topic, String app, int count, long timeout, long ackTimeout, long longPollTimeout, FetchListener listener);

    FetchMessageData fetchPartition(BrokerNode brokerNode, String topic, String app, short partition, int count, long timeout);

    void fetchPartitionAsync(BrokerNode brokerNode, String topic, String app, short partition, int count, long timeout, PartitionFetchListener listener);

    FetchMessageData fetchPartition(BrokerNode brokerNode, String topic, String app, short partition, long index, int count, long timeout);

    void fetchPartitionAsync(BrokerNode brokerNode, String topic, String app, short partition, long index, int count, long timeout, PartitionFetchListener listener);

    // batch

    Map<String, FetchMessageData> batchFetch(BrokerNode brokerNode, List<String> topics, String app, int count, long timeout, long ackTimeout, long longPollTimeout);

    void batchFetchAsync(BrokerNode brokerNode, List<String> topics, String app, int count, long timeout, long ackTimeout, long longPollTimeout, BatchFetchListener listener);

    Table<String, Short, FetchMessageData> batchFetchPartitions(BrokerNode brokerNode, Map<String, Short> partitions, String app, int count, long timeout);

    void batchFetchPartitionsAsync(BrokerNode brokerNode, Map<String, Short> partitions, String app, int count, long timeout, BatchPartitionFetchListener listener);

    Table<String, Short, FetchMessageData> batchFetchPartitions(BrokerNode brokerNode, Table<String, Short, Long> partitions, String app, int count, long timeout);

    void batchFetchPartitionsAsync(BrokerNode brokerNode, Table<String, Short, Long> partitions, String app, int count, long timeout, BatchPartitionFetchListener listener);
}