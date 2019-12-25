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

import org.joyqueue.client.internal.cluster.ClusterManager;
import org.joyqueue.client.internal.cluster.ClusterManagerFactory;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.nameserver.helper.NameServerHelper;
import org.joyqueue.client.internal.producer.config.SenderConfig;
import org.joyqueue.client.internal.producer.feedback.DefaultTxFeedbackManager;
import org.joyqueue.client.internal.producer.feedback.TxFeedbackManagerWrapper;
import org.joyqueue.client.internal.producer.feedback.config.TxFeedbackConfig;
import org.joyqueue.client.internal.producer.transport.ProducerClientManager;
import org.joyqueue.client.internal.producer.transport.ProducerClientManagerFactory;
import org.joyqueue.client.internal.transport.config.TransportConfig;

/**
 * TxFeedbackManagerFactory
 *
 * author: gaohaoxiang
 * date: 2019/1/3
 */
public class TxFeedbackManagerFactory {

    public static TxFeedbackManager create(String address, String app, String token) {
        return create(address, app, token, null, null);
    }

    public static TxFeedbackManager create(String address, String app, String token, String region, String namespace) {
        TxFeedbackConfig txFeedbackConfig = new TxFeedbackConfig();
        txFeedbackConfig.setApp(app);
        NameServerConfig nameServerConfig = NameServerHelper.createConfig(address, app, token, region, namespace);
        return create(txFeedbackConfig, nameServerConfig);
    }

    public static TxFeedbackManager create(TxFeedbackConfig config, NameServerConfig nameServerConfig) {
        return create(config, nameServerConfig, new TransportConfig());
    }

    public static TxFeedbackManager create(TxFeedbackConfig config, NameServerConfig nameServerConfig, TransportConfig transportConfig) {
        ClusterManager clusterManager = ClusterManagerFactory.create(nameServerConfig, transportConfig);
        ProducerClientManager producerClientManager = ProducerClientManagerFactory.create(nameServerConfig, transportConfig);
        return create(config, nameServerConfig, clusterManager, producerClientManager);
    }

    public static TxFeedbackManager create(TxFeedbackConfig config, NameServerConfig nameServerConfig, ClusterManager clusterManager) {
        return create(config, nameServerConfig, new TransportConfig(), clusterManager);
    }

    public static TxFeedbackManager create(TxFeedbackConfig config, NameServerConfig nameServerConfig, TransportConfig transportConfig, ClusterManager clusterManager) {
        ProducerClientManager producerClientManager = ProducerClientManagerFactory.create(nameServerConfig, transportConfig);
        return create(config, nameServerConfig, clusterManager, producerClientManager);
    }

    public static TxFeedbackManager create(TxFeedbackConfig config, NameServerConfig nameServerConfig, ClusterManager clusterManager, ProducerClientManager producerClientManager) {
        MessageSender messageSender = MessageSenderFactory.create(producerClientManager, new SenderConfig());
        DefaultTxFeedbackManager txFeedbackManager = new DefaultTxFeedbackManager(config, nameServerConfig, clusterManager, messageSender);
        return new TxFeedbackManagerWrapper(null, null, messageSender, txFeedbackManager);
    }
}