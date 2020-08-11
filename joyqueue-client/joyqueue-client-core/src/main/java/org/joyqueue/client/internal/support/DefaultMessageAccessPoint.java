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
package org.joyqueue.client.internal.support;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.joyqueue.client.internal.MessageAccessPoint;
import org.joyqueue.client.internal.cluster.ClusterClientManager;
import org.joyqueue.client.internal.cluster.ClusterClientManagerFactory;
import org.joyqueue.client.internal.cluster.ClusterManager;
import org.joyqueue.client.internal.cluster.ClusterManagerFactory;
import org.joyqueue.client.internal.consumer.MessageConsumer;
import org.joyqueue.client.internal.consumer.MessageConsumerFactory;
import org.joyqueue.client.internal.consumer.MessagePoller;
import org.joyqueue.client.internal.consumer.MessagePollerFactory;
import org.joyqueue.client.internal.consumer.config.ConsumerConfig;
import org.joyqueue.client.internal.consumer.transport.ConsumerClientManager;
import org.joyqueue.client.internal.consumer.transport.ConsumerClientManagerFactory;
import org.joyqueue.client.internal.exception.ClientException;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.nameserver.NameServerConfigChecker;
import org.joyqueue.client.internal.producer.MessageProducer;
import org.joyqueue.client.internal.producer.MessageProducerFactory;
import org.joyqueue.client.internal.producer.TxFeedbackManager;
import org.joyqueue.client.internal.producer.TxFeedbackManagerFactory;
import org.joyqueue.client.internal.producer.callback.TxFeedbackCallback;
import org.joyqueue.client.internal.producer.config.ProducerConfig;
import org.joyqueue.client.internal.producer.feedback.config.TxFeedbackConfig;
import org.joyqueue.client.internal.producer.transport.ProducerClientManager;
import org.joyqueue.client.internal.producer.transport.ProducerClientManagerFactory;
import org.joyqueue.client.internal.transport.config.TransportConfig;
import org.joyqueue.client.internal.transport.config.TransportConfigChecker;
import org.joyqueue.toolkit.service.Service;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * DefaultMessageAccessPoint
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public class DefaultMessageAccessPoint extends Service implements MessageAccessPoint {

    private NameServerConfig nameServerConfig;
    private TransportConfig transportConfig;

    private ClusterManager clusterManager;
    private ClusterClientManager clusterClientManager;
    private ProducerClientManager producerClientManager;
    private ConsumerClientManager consumerClientManager;
    private Map<String, TxFeedbackManager> txFeedbackManagerMap = Maps.newHashMap();

    public DefaultMessageAccessPoint(NameServerConfig nameServerConfig, TransportConfig transportConfig) {
        NameServerConfigChecker.check(nameServerConfig);
        TransportConfigChecker.check(transportConfig);

        this.nameServerConfig = nameServerConfig;
        this.transportConfig = transportConfig;
    }

    @Override
    protected void doStop() {
        if (clusterManager != null) {
            clusterManager.stop();
        }
        if (clusterClientManager != null) {
            clusterClientManager.stop();
        }
        if (producerClientManager != null) {
            producerClientManager.stop();
        }
        if (consumerClientManager != null) {
            consumerClientManager.stop();
        }
        for (Map.Entry<String, TxFeedbackManager> entry : txFeedbackManagerMap.entrySet()) {
            entry.getValue().stop();
        }
    }

    @Override
    public MessagePoller createPoller() {
        return createPoller((String) null);
    }

    @Override
    public MessagePoller createPoller(String group) {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setApp(nameServerConfig.getApp());
        consumerConfig.setGroup(group);
        return createPoller(consumerConfig);
    }

    @Override
    public MessagePoller createPoller(ConsumerConfig config) {
        Preconditions.checkArgument(config != null, "config can not be null");

        config.setApp(nameServerConfig.getApp());
        return MessagePollerFactory.create(config, nameServerConfig, getClusterManager(), getClusterClientManager(), getConsumerClientManager());
    }

    @Override
    public synchronized MessageConsumer createConsumer() {
        return createConsumer((String) null);
    }

    @Override
    public synchronized MessageConsumer createConsumer(String group) {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setApp(nameServerConfig.getApp());
        consumerConfig.setGroup(group);
        return createConsumer(consumerConfig);
    }

    @Override
    public synchronized MessageConsumer createConsumer(ConsumerConfig config) {
        Preconditions.checkArgument(config != null, "config can not be null");

        config.setApp(nameServerConfig.getApp());
        return MessageConsumerFactory.create(config, nameServerConfig, getClusterManager(), getClusterClientManager(), getConsumerClientManager());
    }

    @Override
    public synchronized MessageProducer createProducer() {
        ProducerConfig producerConfig = new ProducerConfig();
        producerConfig.setApp(nameServerConfig.getApp());
        return createProducer(producerConfig);
    }

    @Override
    public synchronized MessageProducer createProducer(ProducerConfig config) {
        Preconditions.checkArgument(config != null, "config can not be null");

        config.setApp(nameServerConfig.getApp());
        return MessageProducerFactory.create(config, nameServerConfig, getClusterManager(), getProducerClientManager());
    }

    @Override
    public synchronized void setTransactionCallback(String topic, TxFeedbackCallback callback) {
        TxFeedbackConfig txFeedbackConfig = new TxFeedbackConfig();
        txFeedbackConfig.setApp(nameServerConfig.getApp());
        setTransactionCallback(topic, txFeedbackConfig, callback);
    }

    @Override
    public void setTransactionCallback(String topic, TxFeedbackConfig config, TxFeedbackCallback callback) {
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic can not be null");
        Preconditions.checkArgument(config != null, "config can not be null");
        Preconditions.checkArgument(callback != null, "callback can not be null");

        TxFeedbackManager txFeedbackManager = getTxFeedbackManager(config);
        txFeedbackManager.setTransactionCallback(topic, callback);
        txFeedbackManagerMap.put(topic, txFeedbackManager);
    }

    @Override
    public synchronized void removeTransactionCallback(String topic) {
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic can not be null");
        TxFeedbackManager txFeedbackManager = txFeedbackManagerMap.get(topic);
        if (txFeedbackManager == null) {
            throw new IllegalArgumentException(String.format("%s feedback does not exist", topic));
        }
        txFeedbackManager.removeTransactionCallback(topic);
    }

    protected ClusterManager getClusterManager() {
        if (clusterManager != null) {
            return clusterManager;
        }
        ClusterManager clusterManager = ClusterManagerFactory.create(nameServerConfig, getClusterClientManager());
        try {
            clusterManager.start();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        this.clusterManager = clusterManager;
        return clusterManager;
    }

    protected ClusterClientManager getClusterClientManager() {
        if (clusterClientManager != null) {
            return clusterClientManager;
        }
        ClusterClientManager clusterClientManager = ClusterClientManagerFactory.create(nameServerConfig, transportConfig);
        try {
            clusterClientManager.start();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        this.clusterClientManager = clusterClientManager;
        return clusterClientManager;
    }

    protected ProducerClientManager getProducerClientManager() {
        if (producerClientManager != null) {
            return producerClientManager;
        }
        ProducerClientManager producerClientManager = ProducerClientManagerFactory.create(nameServerConfig, transportConfig);
        try {
            producerClientManager.start();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        this.producerClientManager = producerClientManager;
        return producerClientManager;
    }

    protected ConsumerClientManager getConsumerClientManager() {
        if (consumerClientManager != null) {
            return consumerClientManager;
        }
        ConsumerClientManager consumerClientManager = ConsumerClientManagerFactory.create(nameServerConfig, transportConfig);
        try {
            consumerClientManager.start();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        this.consumerClientManager = consumerClientManager;
        return consumerClientManager;
    }

    protected TxFeedbackManager getTxFeedbackManager(TxFeedbackConfig config) {
        TxFeedbackManager txFeedbackManager = TxFeedbackManagerFactory.create(config, nameServerConfig, getClusterManager(), getProducerClientManager());
        try {
            txFeedbackManager.start();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        return txFeedbackManager;
    }
}
