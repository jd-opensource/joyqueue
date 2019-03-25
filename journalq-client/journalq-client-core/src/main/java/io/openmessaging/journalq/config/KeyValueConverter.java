package io.openmessaging.journalq.config;

import com.jd.journalq.client.internal.consumer.config.ConsumerConfig;
import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.client.internal.producer.config.ProducerConfig;
import com.jd.journalq.client.internal.producer.feedback.config.TxFeedbackConfig;
import com.jd.journalq.client.internal.transport.config.TransportConfig;
import com.jd.journalq.domain.QosLevel;
import io.openmessaging.KeyValue;
import io.openmessaging.journalq.domain.JMQConsumerBuiltinKeys;
import io.openmessaging.journalq.domain.JMQNameServerBuiltinKeys;
import io.openmessaging.journalq.domain.JMQProducerBuiltinKeys;
import io.openmessaging.journalq.domain.JMQTransportBuiltinKeys;
import io.openmessaging.journalq.domain.JMQTxFeedbackBuiltinKeys;

/**
 * KeyValueConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public class KeyValueConverter {

    public static NameServerConfig convertNameServerConfig(KeyValue attributes) {
        NameServerConfig nameServerConfig = new NameServerConfig();
        nameServerConfig.setAddress(attributes.getString(JMQNameServerBuiltinKeys.ACCESS_POINTS));
        nameServerConfig.setApp(attributes.getString(JMQNameServerBuiltinKeys.ACCOUNT_ID));
        nameServerConfig.setToken(attributes.getString(JMQNameServerBuiltinKeys.ACCOUNT_KEY));
        nameServerConfig.setRegion(attributes.getString(JMQNameServerBuiltinKeys.REGION));
        nameServerConfig.setNamespace(attributes.getString(JMQNameServerBuiltinKeys.NAMESPACE));
        nameServerConfig.setUpdateMetadataInterval(KeyValueHelper.getInt(attributes, JMQNameServerBuiltinKeys.METADATA_UPDATE_INTERVAL, nameServerConfig.getUpdateMetadataInterval()));
        nameServerConfig.setTempMetadataInterval(KeyValueHelper.getInt(attributes, JMQNameServerBuiltinKeys.METADATA_TEMP_INTERVAL, nameServerConfig.getTempMetadataInterval()));
        nameServerConfig.setUpdateMetadataThread(KeyValueHelper.getInt(attributes, JMQNameServerBuiltinKeys.METADATA_UPDATE_THREAD, nameServerConfig.getUpdateMetadataThread()));
        nameServerConfig.setUpdateMetadataQueueSize(KeyValueHelper.getInt(attributes, JMQNameServerBuiltinKeys.METADATA_UPDATE_QUEUE_SIZE, nameServerConfig.getUpdateMetadataQueueSize()));
        return nameServerConfig;
    }

    public static TransportConfig convertTransportConfig(KeyValue attributes) {
        TransportConfig transportConfig = new TransportConfig();
        transportConfig.setConnections(KeyValueHelper.getInt(attributes, JMQTransportBuiltinKeys.CONNECTIONS, transportConfig.getConnections()));
        transportConfig.setSoTimeout(KeyValueHelper.getInt(attributes, JMQTransportBuiltinKeys.SO_TIMEOUT, transportConfig.getSoTimeout()));
        transportConfig.setIoThreads(KeyValueHelper.getInt(attributes, JMQTransportBuiltinKeys.IO_THREADS, transportConfig.getIoThreads()));
        transportConfig.setCallbackThreads(KeyValueHelper.getInt(attributes, JMQTransportBuiltinKeys.CALLBACK_THREADS, transportConfig.getCallbackThreads()));
        transportConfig.setChannelMaxIdleTime(KeyValueHelper.getInt(attributes, JMQTransportBuiltinKeys.CHANNEL_MAX_IDLE_TIME, transportConfig.getChannelMaxIdleTime()));
        transportConfig.setHeartbeatInterval(KeyValueHelper.getInt(attributes, JMQTransportBuiltinKeys.HEARTBEAT_INTERVAL, transportConfig.getHeartbeatInterval()));
        transportConfig.setHeartbeatTimeout(KeyValueHelper.getInt(attributes, JMQTransportBuiltinKeys.HEARTBEAT_TIMEOUT, transportConfig.getHeartbeatTimeout()));
        transportConfig.setSoLinger(KeyValueHelper.getInt(attributes, JMQTransportBuiltinKeys.SO_LINGER, transportConfig.getSoLinger()));
        transportConfig.setTcpNoDelay(attributes.getBoolean(JMQTransportBuiltinKeys.CONNECTIONS, transportConfig.isTcpNoDelay()));
        transportConfig.setKeepAlive(attributes.getBoolean(JMQTransportBuiltinKeys.KEEPALIVE, transportConfig.isKeepAlive()));
        transportConfig.setSoTimeout(KeyValueHelper.getInt(attributes, JMQTransportBuiltinKeys.SO_TIMEOUT, transportConfig.getSoTimeout()));
        transportConfig.setSocketBufferSize(KeyValueHelper.getInt(attributes, JMQTransportBuiltinKeys.SOCKET_BUFFER_SIZE, transportConfig.getSocketBufferSize()));
        transportConfig.setMaxOneway(KeyValueHelper.getInt(attributes, JMQTransportBuiltinKeys.MAX_ONEWAY, transportConfig.getMaxOneway()));
        transportConfig.setMaxAsync(KeyValueHelper.getInt(attributes, JMQTransportBuiltinKeys.MAX_ASYNC, transportConfig.getMaxAsync()));
        transportConfig.setNonBlockOneway(attributes.getBoolean(JMQTransportBuiltinKeys.NONBLOCK_ONEWAY, transportConfig.isNonBlockOneway()));
        transportConfig.getRetryPolicy().setMaxRetrys(KeyValueHelper.getInt(attributes, JMQTransportBuiltinKeys.RETRIES, transportConfig.getRetryPolicy().getMaxRetrys()));
        return transportConfig;
    }

    public static ProducerConfig convertProducerConfig(KeyValue attributes) {
        ProducerConfig producerConfig = new ProducerConfig();
        producerConfig.setTimeout(attributes.getLong(JMQProducerBuiltinKeys.TIMEOUT, producerConfig.getTimeout()));
        producerConfig.setProduceTimeout(attributes.getLong(JMQProducerBuiltinKeys.PRODUCE_TIMEOUT, producerConfig.getProduceTimeout()));
        producerConfig.setTransactionTimeout(attributes.getLong(JMQProducerBuiltinKeys.TRANSACTION_TIMEOUT, producerConfig.getTransactionTimeout()));
        producerConfig.setFailover(attributes.getBoolean(JMQProducerBuiltinKeys.FAILOVER, producerConfig.isFailover()));
        producerConfig.getRetryPolicy().setMaxRetrys(KeyValueHelper.getInt(attributes, JMQProducerBuiltinKeys.RETRIES, producerConfig.getRetryPolicy().getMaxRetrys()));
        producerConfig.setQosLevel(QosLevel.valueOf(KeyValueHelper.getInt(attributes, JMQProducerBuiltinKeys.QOSLEVEL, producerConfig.getQosLevel().value())));
        producerConfig.setCompress(attributes.getBoolean(JMQProducerBuiltinKeys.COMPRESS, producerConfig.isCompress()));
        producerConfig.setCompressType(KeyValueHelper.getString(attributes, JMQProducerBuiltinKeys.COMPRESS_TYPE, producerConfig.getCompressType()));
        producerConfig.setCompressThreshold(KeyValueHelper.getInt(attributes, JMQProducerBuiltinKeys.COMPRESS_THRESHOLD, producerConfig.getCompressThreshold()));
        producerConfig.setSelectorType(KeyValueHelper.getString(attributes, JMQProducerBuiltinKeys.SELECTOR_TYPE, producerConfig.getSelectorType()));
        producerConfig.setBusinessIdLengthLimit(KeyValueHelper.getInt(attributes, JMQProducerBuiltinKeys.BUSINESSID_LENGTH_LIMIT, producerConfig.getBusinessIdLengthLimit()));
        producerConfig.setBodyLengthLimit(KeyValueHelper.getInt(attributes, JMQProducerBuiltinKeys.BODY_LENGTH_LIMIT, producerConfig.getBodyLengthLimit()));
        producerConfig.setBatchBodyLengthLimit(KeyValueHelper.getInt(attributes, JMQProducerBuiltinKeys.BATCH_BODY_LENGTH_LIMIT, producerConfig.getBatchBodyLengthLimit()));
        return producerConfig;
    }

    public static ProducerConfig convertProducerConfig(NameServerConfig nameServerConfig, KeyValue attributes) {
        ProducerConfig producerConfig = convertProducerConfig(attributes);
        producerConfig.setApp(nameServerConfig.getApp());
        return producerConfig;
    }

    public static ConsumerConfig convertConsumerConfig(KeyValue attributes) {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setGroup(KeyValueHelper.getString(attributes, JMQConsumerBuiltinKeys.GROUP, consumerConfig.getGroup()));
        consumerConfig.setBatchSize(KeyValueHelper.getInt(attributes, JMQConsumerBuiltinKeys.BATCH_SIZE, consumerConfig.getBatchSize()));
        consumerConfig.setAckTimeout(attributes.getLong(JMQConsumerBuiltinKeys.ACK_TIMEOUT, consumerConfig.getAckTimeout()));
        consumerConfig.setTimeout(attributes.getLong(JMQConsumerBuiltinKeys.TIMEOUT, consumerConfig.getTimeout()));
        consumerConfig.setPollTimeout(attributes.getLong(JMQConsumerBuiltinKeys.POLL_TIMEOUT, consumerConfig.getPollTimeout()));
        consumerConfig.setLongPollTimeout(attributes.getLong(JMQConsumerBuiltinKeys.LONGPOLL_TIMEOUT, consumerConfig.getLongPollTimeout()));
        consumerConfig.setInterval(attributes.getLong(JMQConsumerBuiltinKeys.INTERVAL, consumerConfig.getInterval()));
        consumerConfig.setIdleInterval(attributes.getLong(JMQConsumerBuiltinKeys.IDLE_INTERVAL, consumerConfig.getIdleInterval()));
        consumerConfig.setSessionTimeout(attributes.getLong(JMQConsumerBuiltinKeys.SESSION_TIMEOUT, consumerConfig.getSessionTimeout()));
        consumerConfig.setThread(KeyValueHelper.getInt(attributes, JMQConsumerBuiltinKeys.THREAD, consumerConfig.getThread()));
        consumerConfig.setFailover(attributes.getBoolean(JMQConsumerBuiltinKeys.FAILOVER, consumerConfig.isFailover()));
        consumerConfig.setLoadBalance(attributes.getBoolean(JMQConsumerBuiltinKeys.LOADBALANCE, consumerConfig.isLoadBalance()));
        consumerConfig.setLoadBalanceType(KeyValueHelper.getString(attributes, JMQConsumerBuiltinKeys.LOADBALANCE_TYPE, consumerConfig.getLoadBalanceType()));
        consumerConfig.setBroadcastGroup(KeyValueHelper.getString(attributes, JMQConsumerBuiltinKeys.BROADCAST_GROUP, consumerConfig.getBroadcastGroup()));
        consumerConfig.setBroadcastLocalPath(KeyValueHelper.getString(attributes, JMQConsumerBuiltinKeys.BROADCAST_LOCAL_PATH, consumerConfig.getBroadcastLocalPath()));
        consumerConfig.setBroadcastPersistInterval(KeyValueHelper.getInt(attributes, JMQConsumerBuiltinKeys.BROADCAST_PERSIST_INTERVAL, consumerConfig.getBroadcastPersistInterval()));
        consumerConfig.setBroadcastIndexExpireTime(KeyValueHelper.getInt(attributes, JMQConsumerBuiltinKeys.BROADCAST_INDEX_EXPIRE_TIME, consumerConfig.getBroadcastIndexExpireTime()));
        return consumerConfig;
    }

    public static ConsumerConfig convertConsumerConfig(NameServerConfig nameServerConfig, KeyValue attributes) {
        ConsumerConfig consumerConfig = convertConsumerConfig(attributes);
        consumerConfig.setApp(nameServerConfig.getApp());
        return consumerConfig;
    }

    public static TxFeedbackConfig convertFeedbackConfig(KeyValue attributes) {
        TxFeedbackConfig txFeedbackConfig = new TxFeedbackConfig();
        txFeedbackConfig.setTimeout(attributes.getLong(JMQTxFeedbackBuiltinKeys.TIMEOUT, txFeedbackConfig.getTimeout()));
        txFeedbackConfig.setLongPollTimeout(attributes.getLong(JMQTxFeedbackBuiltinKeys.LONGPOLL_TIMEOUT, txFeedbackConfig.getLongPollTimeout()));
        txFeedbackConfig.setFetchInterval(KeyValueHelper.getInt(attributes, JMQTxFeedbackBuiltinKeys.FETCH_INTERVAL, txFeedbackConfig.getFetchInterval()));
        txFeedbackConfig.setFetchSize(KeyValueHelper.getInt(attributes, JMQTxFeedbackBuiltinKeys.FETCH_SIZE, txFeedbackConfig.getFetchSize()));
        return txFeedbackConfig;
    }

    public static TxFeedbackConfig convertFeedbackConfig(NameServerConfig nameServerConfig, KeyValue attributes) {
        TxFeedbackConfig txFeedbackConfig = convertFeedbackConfig(attributes);
        txFeedbackConfig.setApp(nameServerConfig.getApp());
        return txFeedbackConfig;
    }
}