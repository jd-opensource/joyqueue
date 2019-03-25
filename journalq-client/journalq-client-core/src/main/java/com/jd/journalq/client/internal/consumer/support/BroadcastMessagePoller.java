package com.jd.journalq.client.internal.consumer.support;

import com.jd.journalq.client.internal.cluster.ClusterManager;
import com.jd.journalq.client.internal.consumer.MessagePoller;
import com.jd.journalq.client.internal.consumer.callback.ConsumerListener;
import com.jd.journalq.client.internal.consumer.config.ConsumerConfig;
import com.jd.journalq.client.internal.consumer.domain.ConsumeMessage;
import com.jd.journalq.client.internal.consumer.domain.ConsumeReply;
import com.jd.journalq.client.internal.consumer.transport.ConsumerClientManager;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.service.Service;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * BroadcastMessagePoller
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/14
 */
public class BroadcastMessagePoller extends Service implements MessagePoller {

    private ConsumerConfig config;
    private NameServerConfig nameServerConfig;
    private ClusterManager clusterManager;
    private ConsumerClientManager consumerClientManager;
    private LocalConsumerIndexManager consumerIndexManager;
    private PartitionMessagePoller delegate;

    public BroadcastMessagePoller(ConsumerConfig config, NameServerConfig nameServerConfig, ClusterManager clusterManager, ConsumerClientManager consumerClientManager) {
        Preconditions.checkArgument(StringUtils.isNotBlank(config.getBroadcastGroup()), "consumer.broadcastGroup must be greater than 0");
        Preconditions.checkArgument(StringUtils.isNotBlank(config.getBroadcastLocalPath()), "consumer.broadcastLocalPath must not be null");

        this.config = config;
        this.nameServerConfig = nameServerConfig;
        this.clusterManager = clusterManager;
        this.consumerClientManager = consumerClientManager;
    }

    @Override
    protected void validate() throws Exception {
        consumerIndexManager = new LocalConsumerIndexManager(config, new DefaultConsumerIndexManager(clusterManager, consumerClientManager));
        delegate = new PartitionMessagePoller(config, nameServerConfig, clusterManager, consumerClientManager, consumerIndexManager);
    }

    @Override
    protected void doStart() throws Exception {
        consumerIndexManager.start();
        delegate.start();
    }

    @Override
    protected void doStop() {
        if (consumerIndexManager != null) {
            consumerIndexManager.stop();
        }
        delegate.stop();
    }

    @Override
    public ConsumeMessage pollOnce(String topic) {
        return delegate.pollOnce(topic);
    }

    @Override
    public ConsumeMessage pollOnce(String topic, long timeout, TimeUnit timeoutUnit) {
        return delegate.pollOnce(topic, timeout, timeoutUnit);
    }

    @Override
    public List<ConsumeMessage> poll(String topic) {
        return delegate.poll(topic);
    }

    @Override
    public List<ConsumeMessage> poll(String topic, long timeout, TimeUnit timeoutUnit) {
        return delegate.poll(topic, timeout, timeoutUnit);
    }

    @Override
    public ConsumeMessage pollPartitionOnce(String topic, short partition) {
        return delegate.pollPartitionOnce(topic, partition);
    }

    @Override
    public ConsumeMessage pollPartitionOnce(String topic, short partition, long timeout, TimeUnit timeoutUnit) {
        return delegate.pollPartitionOnce(topic, partition, timeout, timeoutUnit);
    }

    @Override
    public ConsumeMessage pollPartitionOnce(String topic, short partition, long index) {
        return delegate.pollPartitionOnce(topic, partition, index);
    }

    @Override
    public ConsumeMessage pollPartitionOnce(String topic, short partition, long index, long timeout, TimeUnit timeoutUnit) {
        return delegate.pollPartitionOnce(topic, partition, index, timeout, timeoutUnit);
    }

    @Override
    public List<ConsumeMessage> pollPartition(String topic, short partition) {
        return delegate.pollPartition(topic, partition);
    }

    @Override
    public List<ConsumeMessage> pollPartition(String topic, short partition, long timeout, TimeUnit timeoutUnit) {
        return delegate.pollPartition(topic, partition, timeout, timeoutUnit);
    }

    @Override
    public List<ConsumeMessage> pollPartition(String topic, short partition, long index) {
        return delegate.pollPartition(topic, partition, index);
    }

    @Override
    public List<ConsumeMessage> pollPartition(String topic, short partition, long index, long timeout, TimeUnit timeoutUnit) {
        return delegate.pollPartition(topic, partition, index, timeout, timeoutUnit);
    }

    @Override
    public void pollPartitionAsync(String topic, short partition, ConsumerListener listener) {
        delegate.pollPartitionAsync(topic, partition, listener);
    }

    @Override
    public void pollPartitionAsync(String topic, short partition, long timeout, TimeUnit timeoutUnit, ConsumerListener listener) {
        delegate.pollPartitionAsync(topic, partition, timeout, timeoutUnit, listener);
    }

    @Override
    public void pollPartitionAsync(String topic, short partition, long index, ConsumerListener listener) {
        delegate.pollPartitionAsync(topic, partition, index, listener);
    }

    @Override
    public void pollPartitionAsync(String topic, short partition, long index, long timeout, TimeUnit timeoutUnit, ConsumerListener listener) {
        delegate.pollPartitionAsync(topic, partition, index, timeout, timeoutUnit, listener);
    }

    @Override
    public void pollAsync(String topic, ConsumerListener listener) {
        delegate.pollAsync(topic, listener);
    }

    @Override
    public void pollAsync(String topic, long timeout, TimeUnit timeoutUnit, ConsumerListener listener) {
        delegate.pollAsync(topic, timeout, timeoutUnit, listener);
    }

    @Override
    public JMQCode reply(String topic, List<ConsumeReply> replyList) {
        return delegate.reply(topic, replyList);
    }

    @Override
    public JMQCode replyOnce(String topic, ConsumeReply reply) {
        return delegate.replyOnce(topic, reply);
    }

    @Override
    public TopicMetadata getTopicMetadata(String topic) {
        return delegate.getTopicMetadata(topic);
    }
}