package com.jd.journalq.client.internal.consumer.container;

import com.jd.journalq.client.internal.cluster.ClusterClientManager;
import com.jd.journalq.client.internal.cluster.ClusterClientManagerFactory;
import com.jd.journalq.client.internal.cluster.ClusterManager;
import com.jd.journalq.client.internal.cluster.ClusterManagerFactory;
import com.jd.journalq.client.internal.consumer.MessageListenerContainer;
import com.jd.journalq.client.internal.consumer.config.ConsumerConfig;
import com.jd.journalq.client.internal.consumer.transport.ConsumerClientManager;
import com.jd.journalq.client.internal.consumer.transport.ConsumerClientManagerFactory;
import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.client.internal.nameserver.helper.NameServerHelper;
import com.jd.journalq.client.internal.transport.config.TransportConfig;

/**
 * MessageListenerContainerFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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

    public static MessageListenerContainer create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, ClusterClientManager clusterClientManager, ConsumerClientManager consumerClientManager) {
        ClusterManager clusterManager = ClusterManagerFactory.create(nameServerConfig, clusterClientManager);
        DefaultMessageListenerContainer messageConsumerContainer = new DefaultMessageListenerContainer(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager);
        return new MessageListenerContainerWrapper(consumerConfig, nameServerConfig, clusterManager, null, null, messageConsumerContainer);
    }

    public static MessageListenerContainer create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, ClusterManager clusterManager,
                                                  ClusterClientManager clusterClientManager, ConsumerClientManager consumerClientManager) {
        return new DefaultMessageListenerContainer(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager);
    }
}