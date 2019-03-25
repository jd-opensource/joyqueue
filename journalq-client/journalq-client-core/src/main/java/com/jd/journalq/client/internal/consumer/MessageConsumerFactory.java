package com.jd.journalq.client.internal.consumer;

import com.jd.journalq.client.internal.cluster.ClusterClientManager;
import com.jd.journalq.client.internal.cluster.ClusterClientManagerFactory;
import com.jd.journalq.client.internal.cluster.ClusterManager;
import com.jd.journalq.client.internal.cluster.ClusterManagerFactory;
import com.jd.journalq.client.internal.consumer.config.ConsumerConfig;
import com.jd.journalq.client.internal.consumer.support.DefaultMessageConsumer;
import com.jd.journalq.client.internal.consumer.support.MessageConsumerWrapper;
import com.jd.journalq.client.internal.consumer.transport.ConsumerClientManager;
import com.jd.journalq.client.internal.consumer.transport.ConsumerClientManagerFactory;
import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.client.internal.nameserver.helper.NameServerHelper;
import com.jd.journalq.client.internal.transport.config.TransportConfig;

/**
 * MessageConsumerFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/10
 */
public class MessageConsumerFactory {

    public static MessageConsumer create(String address, String app, String token) {
        return create(address, app, token, null, null);
    }

    public static MessageConsumer create(String address, String app, String token, String region) {
        return create(address, app, token, region, null);
    }

    public static MessageConsumer create(String address, String app, String token, String region, String namespace) {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setApp(app);
        NameServerConfig nameServerConfig = NameServerHelper.createConfig(address, app, token, region, namespace);
        return create(consumerConfig, nameServerConfig);
    }

    public static MessageConsumer create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig) {
        return create(consumerConfig, nameServerConfig, new TransportConfig());
    }

    public static MessageConsumer create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, TransportConfig transportConfig) {
        ClusterClientManager clusterClientManager = ClusterClientManagerFactory.create(nameServerConfig, transportConfig);
        ClusterManager clusterManager = ClusterManagerFactory.create(nameServerConfig, clusterClientManager);
        ConsumerClientManager consumerClientManager = ConsumerClientManagerFactory.create(nameServerConfig, transportConfig);
        DefaultMessageConsumer messageConsumer = new DefaultMessageConsumer(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager);
        return new MessageConsumerWrapper(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager, messageConsumer);
    }

    public static MessageConsumer create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, ConsumerClientManager consumerClientManager) {
        return create(consumerConfig, nameServerConfig, new TransportConfig(), consumerClientManager);
    }

    public static MessageConsumer create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, TransportConfig transportConfig, ConsumerClientManager consumerClientManager) {
        ClusterClientManager clusterClientManager = ClusterClientManagerFactory.create(nameServerConfig, transportConfig);
        ClusterManager clusterManager = ClusterManagerFactory.create(nameServerConfig, clusterClientManager);
        DefaultMessageConsumer messageConsumer = new DefaultMessageConsumer(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager);
        return new MessageConsumerWrapper(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, null, messageConsumer);
    }

    public static MessageConsumer create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, ClusterClientManager clusterClientManager, ConsumerClientManager consumerClientManager) {
        ClusterManager clusterManager = ClusterManagerFactory.create(nameServerConfig, clusterClientManager);
        DefaultMessageConsumer messageConsumer = new DefaultMessageConsumer(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager);
        return new MessageConsumerWrapper(consumerConfig, nameServerConfig, clusterManager, null, null, messageConsumer);
    }

    public static MessageConsumer create(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, ClusterManager clusterManager,
                                       ClusterClientManager clusterClientManager, ConsumerClientManager consumerClientManager) {
        return new DefaultMessageConsumer(consumerConfig, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager);
    }
}