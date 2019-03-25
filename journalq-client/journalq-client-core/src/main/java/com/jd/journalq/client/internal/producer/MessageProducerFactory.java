package com.jd.journalq.client.internal.producer;

import com.jd.journalq.client.internal.cluster.ClusterClientManager;
import com.jd.journalq.client.internal.cluster.ClusterManager;
import com.jd.journalq.client.internal.cluster.ClusterManagerFactory;
import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.client.internal.nameserver.helper.NameServerHelper;
import com.jd.journalq.client.internal.producer.config.ProducerConfig;
import com.jd.journalq.client.internal.producer.support.DefaultMessageProducer;
import com.jd.journalq.client.internal.producer.support.MessageProducerWrapper;
import com.jd.journalq.client.internal.producer.transport.ProducerClientManager;
import com.jd.journalq.client.internal.producer.transport.ProducerClientManagerFactory;
import com.jd.journalq.client.internal.transport.config.TransportConfig;

/**
 * MessageProducerFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/27
 */
public class MessageProducerFactory {

    public static MessageProducer create(String address, String app, String token) {
        return create(address, app, token, null, null);
    }

    public static MessageProducer create(String address, String app, String token, String region) {
        return create(address, app, token, region, null);
    }

    public static MessageProducer create(String address, String app, String token, String region, String namespace) {
        ProducerConfig producerConfig = new ProducerConfig();
        producerConfig.setApp(app);
        NameServerConfig nameServerConfig = NameServerHelper.createConfig(address, app, token, region, namespace);
        return create(producerConfig, nameServerConfig);
    }

    public static MessageProducer create(ProducerConfig producerConfig, NameServerConfig nameServerConfig) {
        return create(producerConfig, nameServerConfig, new TransportConfig());
    }

    public static MessageProducer create(ProducerConfig producerConfig, NameServerConfig nameServerConfig, TransportConfig transportConfig) {
        ClusterManager clusterManager = ClusterManagerFactory.create(nameServerConfig, transportConfig);
        ProducerClientManager producerClientManager = ProducerClientManagerFactory.create(nameServerConfig, transportConfig);
        DefaultMessageProducer messageProducer = new DefaultMessageProducer(producerConfig, nameServerConfig, clusterManager, producerClientManager);
        return new MessageProducerWrapper(clusterManager, producerClientManager, messageProducer);
    }

    public static MessageProducer create(ProducerConfig producerConfig, NameServerConfig nameServerConfig, ProducerClientManager producerClientManager) {
        return create(producerConfig, nameServerConfig, new TransportConfig(), producerClientManager);
    }

    public static MessageProducer create(ProducerConfig producerConfig, NameServerConfig nameServerConfig, TransportConfig transportConfig, ProducerClientManager producerClientManager) {
        ClusterManager clusterManager = ClusterManagerFactory.create(nameServerConfig, transportConfig);
        DefaultMessageProducer messageProducer = new DefaultMessageProducer(producerConfig, nameServerConfig, clusterManager, producerClientManager);
        return new MessageProducerWrapper(clusterManager, null, messageProducer);
    }

    public static MessageProducer create(ProducerConfig producerConfig, NameServerConfig nameServerConfig, ClusterClientManager clusterClientManager, ProducerClientManager producerClientManager) {
        ClusterManager clusterManager = ClusterManagerFactory.create(nameServerConfig, clusterClientManager);
        DefaultMessageProducer messageProducer = new DefaultMessageProducer(producerConfig, nameServerConfig, clusterManager, producerClientManager);
        return new MessageProducerWrapper(clusterManager, null, messageProducer);
    }

    public static MessageProducer create(ProducerConfig producerConfig, NameServerConfig nameServerConfig, ClusterManager clusterManager, ProducerClientManager producerClientManager) {
        return new DefaultMessageProducer(producerConfig, nameServerConfig, clusterManager, producerClientManager);
    }
}