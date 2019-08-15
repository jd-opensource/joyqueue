package io.chubao.joyqueue.client.internal.consumer.container;

import io.chubao.joyqueue.client.internal.cluster.ClusterClientManager;
import io.chubao.joyqueue.client.internal.cluster.ClusterClientManagerFactory;
import io.chubao.joyqueue.client.internal.cluster.ClusterManager;
import io.chubao.joyqueue.client.internal.cluster.ClusterManagerFactory;
import io.chubao.joyqueue.client.internal.consumer.MessageListenerContainer;
import io.chubao.joyqueue.client.internal.consumer.config.ConsumerConfig;
import io.chubao.joyqueue.client.internal.consumer.transport.ConsumerClientManager;
import io.chubao.joyqueue.client.internal.consumer.transport.ConsumerClientManagerFactory;
import io.chubao.joyqueue.client.internal.nameserver.NameServerConfig;
import io.chubao.joyqueue.client.internal.nameserver.helper.NameServerHelper;
import io.chubao.joyqueue.client.internal.transport.config.TransportConfig;

/**
 * MessageListenerContainerFactory
 *
 * author: gaohaoxiang
 * date: 2018/12/27
 */
@Deprecated
public class MessageListenerContainerFactory {

    public static MessageListenerContainer create(String address, String app, String token) {
        return create(address, app, token, null, null);
    }

    public static MessageListenerContainer create(String address, String app, String token, String region) {
        return create(address, app, token, region, null);
    }

    public static MessageListenerContainer create(String address, String app, String token, String region, String namespace) {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setApp(app);
        NameServerConfig nameServerConfig = NameServerHelper.createConfig(address, app, token, region, namespace);
        return create(consumerConfig, nameServerConfig);
    }

    public static MessageListenerContainer create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig) {
        return create(consumerConfig, nameServerConfig, new TransportConfig());
    }

    public static MessageListenerContainer create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, TransportConfig transportConfig) {
        ClusterClientManager clusterClientManager = ClusterClientManagerFactory.create(nameServerConfig, transportConfig);
        ClusterManager clusterManager = ClusterManagerFactory.create(nameServerConfig, clusterClientManager);
        ConsumerClientManager consumerClientManager = ConsumerClientManagerFactory.create(nameServerConfig, transportConfig);
        DefaultMessageListenerContainer messageConsumerContainer = new DefaultMessageListenerContainer(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager);
        return new MessageListenerContainerWrapper(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager, messageConsumerContainer);
    }

    public static MessageListenerContainer create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, ConsumerClientManager consumerClientManager) {
        return create(consumerConfig, nameServerConfig, new TransportConfig(), consumerClientManager);
    }

    public static MessageListenerContainer create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, TransportConfig transportConfig, ConsumerClientManager consumerClientManager) {
        ClusterClientManager clusterClientManager = ClusterClientManagerFactory.create(nameServerConfig, transportConfig);
        ClusterManager clusterManager = ClusterManagerFactory.create(nameServerConfig, clusterClientManager);
        DefaultMessageListenerContainer messageConsumerContainer = new DefaultMessageListenerContainer(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager);
        return new MessageListenerContainerWrapper(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, null, messageConsumerContainer);
    }

    public static MessageListenerContainer create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig,
                                                  ClusterClientManager clusterClientManager, ConsumerClientManager consumerClientManager) {
        ClusterManager clusterManager = ClusterManagerFactory.create(nameServerConfig, clusterClientManager);
        DefaultMessageListenerContainer messageConsumerContainer = new DefaultMessageListenerContainer(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager);
        return new MessageListenerContainerWrapper(consumerConfig, nameServerConfig, clusterManager, null, null, messageConsumerContainer);
    }

    public static MessageListenerContainer create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, ClusterManager clusterManager,
                                                  ClusterClientManager clusterClientManager, ConsumerClientManager consumerClientManager) {
        return new DefaultMessageListenerContainer(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager);
    }
}