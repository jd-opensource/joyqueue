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
import org.joyqueue.client.internal.consumer.support.BroadcastMessagePoller;
import org.joyqueue.client.internal.consumer.support.DefaultMessagePoller;
import org.joyqueue.client.internal.consumer.support.MessagePollerWrapper;
import org.joyqueue.client.internal.consumer.support.PartitionMessagePoller;
import org.joyqueue.client.internal.consumer.transport.ConsumerClientManager;
import org.joyqueue.client.internal.consumer.transport.ConsumerClientManagerFactory;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.nameserver.helper.NameServerHelper;
import org.joyqueue.client.internal.transport.config.TransportConfig;

/**
 * MessagePollerFactory
 *
 * author: gaohaoxiang
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