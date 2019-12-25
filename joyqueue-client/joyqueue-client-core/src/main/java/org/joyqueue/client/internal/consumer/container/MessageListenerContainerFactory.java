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
package org.joyqueue.client.internal.consumer.container;

import org.joyqueue.client.internal.cluster.ClusterClientManager;
import org.joyqueue.client.internal.cluster.ClusterClientManagerFactory;
import org.joyqueue.client.internal.cluster.ClusterManager;
import org.joyqueue.client.internal.cluster.ClusterManagerFactory;
import org.joyqueue.client.internal.consumer.MessageListenerContainer;
import org.joyqueue.client.internal.consumer.config.ConsumerConfig;
import org.joyqueue.client.internal.consumer.transport.ConsumerClientManager;
import org.joyqueue.client.internal.consumer.transport.ConsumerClientManagerFactory;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.nameserver.helper.NameServerHelper;
import org.joyqueue.client.internal.transport.config.TransportConfig;

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