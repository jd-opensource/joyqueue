package io.chubao.joyqueue.client.internal.support;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import io.chubao.joyqueue.client.internal.MessageAccessPoint;
import io.chubao.joyqueue.client.internal.cluster.ClusterClientManager;
import io.chubao.joyqueue.client.internal.cluster.ClusterClientManagerFactory;
import io.chubao.joyqueue.client.internal.cluster.ClusterManager;
import io.chubao.joyqueue.client.internal.cluster.ClusterManagerFactory;
import io.chubao.joyqueue.client.internal.consumer.MessageConsumer;
import io.chubao.joyqueue.client.internal.consumer.MessageConsumerFactory;
import io.chubao.joyqueue.client.internal.consumer.MessagePoller;
import io.chubao.joyqueue.client.internal.consumer.MessagePollerFactory;
import io.chubao.joyqueue.client.internal.consumer.config.ConsumerConfig;
import io.chubao.joyqueue.client.internal.consumer.transport.ConsumerClientManager;
import io.chubao.joyqueue.client.internal.consumer.transport.ConsumerClientManagerFactory;
import io.chubao.joyqueue.client.internal.exception.ClientException;
import io.chubao.joyqueue.client.internal.nameserver.NameServerConfig;
import io.chubao.joyqueue.client.internal.nameserver.NameServerConfigChecker;
import io.chubao.joyqueue.client.internal.producer.MessageProducer;
import io.chubao.joyqueue.client.internal.producer.MessageProducerFactory;
import io.chubao.joyqueue.client.internal.producer.TxFeedbackManager;
import io.chubao.joyqueue.client.internal.producer.TxFeedbackManagerFactory;
import io.chubao.joyqueue.client.internal.producer.callback.TxFeedbackCallback;
import io.chubao.joyqueue.client.internal.producer.config.ProducerConfig;
import io.chubao.joyqueue.client.internal.producer.feedback.config.TxFeedbackConfig;
import io.chubao.joyqueue.client.internal.producer.transport.ProducerClientManager;
import io.chubao.joyqueue.client.internal.producer.transport.ProducerClientManagerFactory;
import io.chubao.joyqueue.client.internal.transport.config.TransportConfig;
import io.chubao.joyqueue.client.internal.transport.config.TransportConfigChecker;
import io.chubao.joyqueue.toolkit.service.Service;
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