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
package org.joyqueue.client.internal.producer;

import org.joyqueue.client.internal.cluster.ClusterClientManager;
import org.joyqueue.client.internal.cluster.ClusterManager;
import org.joyqueue.client.internal.cluster.ClusterManagerFactory;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.nameserver.helper.NameServerHelper;
import org.joyqueue.client.internal.producer.config.ProducerConfig;
import org.joyqueue.client.internal.producer.support.DefaultMessageProducer;
import org.joyqueue.client.internal.producer.support.MessageProducerWrapper;
import org.joyqueue.client.internal.producer.transport.ProducerClientManager;
import org.joyqueue.client.internal.producer.transport.ProducerClientManagerFactory;
import org.joyqueue.client.internal.transport.config.TransportConfig;

/**
 * MessageProducerFactory
 *
 * author: gaohaoxiang
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