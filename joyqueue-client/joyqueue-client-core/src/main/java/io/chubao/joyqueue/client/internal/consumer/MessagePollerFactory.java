package io.chubao.joyqueue.client.internal.consumer;

import io.chubao.joyqueue.client.internal.cluster.ClusterClientManager;
import io.chubao.joyqueue.client.internal.cluster.ClusterClientManagerFactory;
import io.chubao.joyqueue.client.internal.cluster.ClusterManager;
import io.chubao.joyqueue.client.internal.cluster.ClusterManagerFactory;
import io.chubao.joyqueue.client.internal.consumer.config.ConsumerConfig;
import io.chubao.joyqueue.client.internal.consumer.support.BroadcastMessagePoller;
import io.chubao.joyqueue.client.internal.consumer.support.DefaultMessagePoller;
import io.chubao.joyqueue.client.internal.consumer.support.MessagePollerWrapper;
import io.chubao.joyqueue.client.internal.consumer.support.PartitionMessagePoller;
import io.chubao.joyqueue.client.internal.consumer.transport.ConsumerClientManager;
import io.chubao.joyqueue.client.internal.consumer.transport.ConsumerClientManagerFactory;
import io.chubao.joyqueue.client.internal.nameserver.NameServerConfig;
import io.chubao.joyqueue.client.internal.nameserver.helper.NameServerHelper;
import io.chubao.joyqueue.client.internal.transport.config.TransportConfig;

/**
 * MessagePollerFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/27
 */
public class MessagePollerFactory {

    public static MessagePoller create(String address, String app, String token) {
        return create(address, app, token, null, null);
    }

    public static MessagePoller create(String address, String app, String token, String region) {
        return create(address, app, token, region, null);
    }

    public static MessagePoller create(String address, String app, String token, String region, String namespace) {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setApp(app);
        NameServerConfig nameServerConfig = NameServerHelper.createConfig(address, app, token, region, namespace);
        return create(consumerConfig, nameServerConfig);
    }

    public static MessagePoller create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig) {
        return create(consumerConfig, nameServerConfig, new TransportConfig());
    }

    public static MessagePoller create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, TransportConfig transportConfig) {
        ClusterClientManager clusterClientManager = ClusterClientManagerFactory.create(nameServerConfig, transportConfig);
        ClusterManager clusterManager = ClusterManagerFactory.create(nameServerConfig, clusterClientManager);
        ConsumerClientManager consumerClientManager = ConsumerClientManagerFactory.create(nameServerConfig, transportConfig);
        DefaultMessagePoller messageConsumer = new DefaultMessagePoller(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager);
        return new MessagePollerWrapper(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager, messageConsumer);
    }

    public static MessagePoller create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, ConsumerClientManager consumerClientManager) {
        return create(consumerConfig, nameServerConfig, new TransportConfig(), consumerClientManager);
    }

    public static MessagePoller create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, TransportConfig transportConfig, ConsumerClientManager consumerClientManager) {
        ClusterClientManager clusterClientManager = ClusterClientManagerFactory.create(nameServerConfig, transportConfig);
        ClusterManager clusterManager = ClusterManagerFactory.create(nameServerConfig, clusterClientManager);
        DefaultMessagePoller messageConsumer = new DefaultMessagePoller(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager);
        return new MessagePollerWrapper(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, null, messageConsumer);
    }

    public static MessagePoller create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, ClusterClientManager clusterClientManager, ConsumerClientManager consumerClientManager) {
        ClusterManager clusterManager = ClusterManagerFactory.create(nameServerConfig, clusterClientManager);
        DefaultMessagePoller messageConsumer = new DefaultMessagePoller(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager);
        return new MessagePollerWrapper(consumerConfig, nameServerConfig, clusterManager, null, null, messageConsumer);
    }

    public static MessagePoller create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, ClusterManager clusterManager,
                                       ClusterClientManager clusterClientManager, ConsumerClientManager consumerClientManager) {
        return new DefaultMessagePoller(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager);
    }

    public static MessagePoller createBroadcastPoller(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, ClusterManager clusterManager, ConsumerClientManager consumerClientManager) {
        return new BroadcastMessagePoller(consumerConfig, nameServerConfig, clusterManager, consumerClientManager);
    }

    public static MessagePoller createPartitionPoller(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig,
                                                      ClusterManager clusterManager, ConsumerClientManager consumerClientManager, ConsumerIndexManager consumerIndexManager) {
        return new PartitionMessagePoller(consumerConfig, nameServerConfig, clusterManager, consumerClientManager, consumerIndexManager);
    }
}