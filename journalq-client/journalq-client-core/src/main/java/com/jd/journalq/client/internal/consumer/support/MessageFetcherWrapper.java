package com.jd.journalq.client.internal.consumer.support;

import com.google.common.collect.Table;
import com.jd.journalq.client.internal.consumer.MessageFetcher;
import com.jd.journalq.client.internal.consumer.callback.BatchFetchListener;
import com.jd.journalq.client.internal.consumer.callback.BatchPartitionFetchListener;
import com.jd.journalq.client.internal.consumer.callback.FetchListener;
import com.jd.journalq.client.internal.consumer.callback.PartitionFetchListener;
import com.jd.journalq.client.internal.consumer.domain.FetchMessageData;
import com.jd.journalq.client.internal.consumer.transport.ConsumerClientManager;
import com.jd.journalq.network.domain.BrokerNode;
import com.jd.journalq.toolkit.service.Service;

import java.util.List;
import java.util.Map;

/**
 * MessageFetcherWrapper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/27
 */
public class MessageFetcherWrapper extends Service implements MessageFetcher {

    private ConsumerClientManager consumerClientManager;
    private MessageFetcher delegate;

    public MessageFetcherWrapper(ConsumerClientManager consumerClientManager, MessageFetcher delegate) {
        this.consumerClientManager = consumerClientManager;
        this.delegate = delegate;
    }

    @Override
    protected void doStart() throws Exception {
        consumerClientManager.start();
        delegate.start();
    }

    @Override
    protected void doStop() {
        delegate.stop();
        consumerClientManager.stop();
    }

    @Override
    public FetchMessageData fetch(BrokerNode brokerNode, String topic, String app, int count, long timeout, long ackTimeout, long longPollTimeout) {
        return delegate.fetch(brokerNode, topic, app, count, timeout, ackTimeout, longPollTimeout);
    }

    @Override
    public void asyncFetch(BrokerNode brokerNode, String topic, String app, int count, long timeout, long ackTimeout, long longPollTimeout, FetchListener listener) {
        delegate.asyncFetch(brokerNode, topic, app, count, timeout, ackTimeout, longPollTimeout, listener);
    }

    @Override
    public FetchMessageData fetchPartition(BrokerNode brokerNode, String topic, String app, short partition, int count, long timeout) {
        return delegate.fetchPartition(brokerNode, topic, app, partition, count, timeout);
    }

    @Override
    public void fetchPartitionAsync(BrokerNode brokerNode, String topic, String app, short partition, int count, long timeout, PartitionFetchListener listener) {
        delegate.fetchPartitionAsync(brokerNode, topic, app, partition, count, count, listener);
    }

    @Override
    public FetchMessageData fetchPartition(BrokerNode brokerNode, String topic, String app, short partition, long index, int count, long timeout) {
        return delegate.fetchPartition(brokerNode, topic, app, partition, index, count, timeout);
    }

    @Override
    public void fetchPartitionAsync(BrokerNode brokerNode, String topic, String app, short partition, long index, int count, long timeout, PartitionFetchListener listener) {
        delegate.fetchPartitionAsync(brokerNode, topic, app, partition, index, count, timeout, listener);
    }

    @Override
    public Map<String, FetchMessageData> batchFetch(BrokerNode brokerNode, List<String> topics, String app, int count, long timeout, long ackTimeout, long longPollTimeout) {
        return delegate.batchFetch(brokerNode, topics, app, count, timeout, ackTimeout, longPollTimeout);
    }

    @Override
    public void batchFetchAsync(BrokerNode brokerNode, List<String> topics, String app, int count, long timeout, long ackTimeout, long longPollTimeout, BatchFetchListener listener) {
        delegate.batchFetchAsync(brokerNode, topics, app, count, timeout, ackTimeout, longPollTimeout, listener);
    }

    @Override
    public Table<String, Short, FetchMessageData> batchFetchPartitions(BrokerNode brokerNode, Map<String, Short> partitions, String app, int count, long timeout) {
        return delegate.batchFetchPartitions(brokerNode, partitions, app, count, timeout);
    }

    @Override
    public void batchFetchPartitionsAsync(BrokerNode brokerNode, Map<String, Short> partitions, String app, int count, long timeout, BatchPartitionFetchListener listener) {
        delegate.batchFetchPartitionsAsync(brokerNode, partitions, app, count, timeout, listener);
    }

    @Override
    public Table<String, Short, FetchMessageData> batchFetchPartitions(BrokerNode brokerNode, Table<String, Short, Long> partitions, String app, int count, long timeout) {
        return delegate.batchFetchPartitions(brokerNode, partitions, app, count, timeout);
    }

    @Override
    public void batchFetchPartitionsAsync(BrokerNode brokerNode, Table<String, Short, Long> partitions, String app, int count, long timeout, BatchPartitionFetchListener listener) {
        delegate.batchFetchPartitionsAsync(brokerNode, partitions, app, count, timeout, listener);
    }
}