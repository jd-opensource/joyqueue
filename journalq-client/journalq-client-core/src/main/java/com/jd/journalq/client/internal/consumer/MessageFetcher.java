package com.jd.journalq.client.internal.consumer;

import com.google.common.collect.Table;
import com.jd.journalq.client.internal.consumer.callback.BatchFetchListener;
import com.jd.journalq.client.internal.consumer.callback.BatchPartitionFetchListener;
import com.jd.journalq.client.internal.consumer.callback.FetchListener;
import com.jd.journalq.client.internal.consumer.callback.PartitionFetchListener;
import com.jd.journalq.client.internal.consumer.domain.FetchMessageData;
import com.jd.journalq.common.network.domain.BrokerNode;
import com.jd.journalq.toolkit.lang.LifeCycle;

import java.util.List;
import java.util.Map;

/**
 * MessageFetcher
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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