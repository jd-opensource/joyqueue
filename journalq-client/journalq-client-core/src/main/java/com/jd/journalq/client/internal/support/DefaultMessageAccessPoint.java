/**
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
package com.jd.journalq.client.internal.support;

import com.google.common.collect.Maps;
import com.jd.journalq.client.internal.MessageAccessPoint;
import com.jd.journalq.client.internal.cluster.ClusterClientManager;
import com.jd.journalq.client.internal.cluster.ClusterClientManagerFactory;
import com.jd.journalq.client.internal.cluster.ClusterManager;
import com.jd.journalq.client.internal.cluster.ClusterManagerFactory;
import com.jd.journalq.client.internal.consumer.MessageConsumer;
import com.jd.journalq.client.internal.consumer.MessageConsumerFactory;
import com.jd.journalq.client.internal.consumer.MessagePoller;
import com.jd.journalq.client.internal.consumer.MessagePollerFactory;
import com.jd.journalq.client.internal.consumer.config.ConsumerConfig;
import com.jd.journalq.client.internal.consumer.transport.ConsumerClientManager;
import com.jd.journalq.client.internal.consumer.transport.ConsumerClientManagerFactory;
import com.jd.journalq.client.internal.exception.ClientException;
import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.client.internal.nameserver.NameServerConfigChecker;
import com.jd.journalq.client.internal.producer.MessageProducer;
import com.jd.journalq.client.internal.producer.MessageProducerFactory;
import com.jd.journalq.client.internal.producer.TxFeedbackManager;
import com.jd.journalq.client.internal.producer.TxFeedbackManagerFactory;
import com.jd.journalq.client.internal.producer.callback.TxFeedbackCallback;
import com.jd.journalq.client.internal.producer.config.ProducerConfig;
import com.jd.journalq.client.internal.producer.feedback.config.TxFeedbackConfig;
import com.jd.journalq.client.internal.producer.transport.ProducerClientManager;
import com.jd.journalq.client.internal.producer.transport.ProducerClientManagerFactory;
import com.jd.journalq.client.internal.transport.config.TransportConfig;
import com.jd.journalq.client.internal.transport.config.TransportConfigChecker;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.service.Service;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * DefaultMessageAccessPoint
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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
            throw new IllegalArgumentException(String.format("%s feedback is not exist", topic));
        }
        txFeedbackManager.removeTransactionCallback(topic);
    }

    protected ClusterManager getClusterManager() {
        if (clusterManager != null) {
            return clusterManager;
        }
        clusterManager = ClusterManagerFactory.create(nameServerConfig, getClusterClientManager());
        try {
            clusterManager.start();
        } catch (Exception e) {
            clusterManager = null;
            throw new ClientException(e);
        }
        return clusterManager;
    }

    protected ClusterClientManager getClusterClientManager() {
        if (clusterClientManager != null) {
            return clusterClientManager;
        }
        clusterClientManager = ClusterClientManagerFactory.create(nameServerConfig, transportConfig);
        try {
            clusterClientManager.start();
        } catch (Exception e) {
            clusterClientManager = null;
            throw new ClientException(e);
        }
        return clusterClientManager;
    }

    protected ProducerClientManager getProducerClientManager() {
        if (producerClientManager != null) {
            return producerClientManager;
        }
        producerClientManager = ProducerClientManagerFactory.create(nameServerConfig, transportConfig);
        try {
            producerClientManager.start();
        } catch (Exception e) {
            producerClientManager = null;
            throw new ClientException(e);
        }
        return producerClientManager;
    }

    protected ConsumerClientManager getConsumerClientManager() {
        if (consumerClientManager != null) {
            return consumerClientManager;
        }
        consumerClientManager = ConsumerClientManagerFactory.create(nameServerConfig, transportConfig);
        try {
            consumerClientManager.start();
        } catch (Exception e) {
            consumerClientManager = null;
            throw new ClientException(e);
        }
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