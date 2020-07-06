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

import org.joyqueue.client.internal.cluster.ClusterClientManager;
import org.joyqueue.client.internal.cluster.ClusterClientManagerFactory;
import org.joyqueue.client.internal.cluster.ClusterManager;
import org.joyqueue.client.internal.cluster.ClusterManagerFactory;
import org.joyqueue.client.internal.consumer.config.ConsumerConfig;
import org.joyqueue.client.internal.consumer.support.DefaultMessageConsumer;
import org.joyqueue.client.internal.consumer.support.MessageConsumerWrapper;
import org.joyqueue.client.internal.consumer.transport.ConsumerClientManager;
import org.joyqueue.client.internal.consumer.transport.ConsumerClientManagerFactory;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.nameserver.helper.NameServerHelper;
import org.joyqueue.client.internal.transport.config.TransportConfig;

/**
 * MessageConsumerFactory
 *
 * author: gaohaoxiang
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