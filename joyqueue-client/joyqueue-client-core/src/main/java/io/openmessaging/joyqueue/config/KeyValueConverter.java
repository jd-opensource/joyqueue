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
package io.openmessaging.joyqueue.config;

import io.openmessaging.KeyValue;
import io.openmessaging.joyqueue.domain.JoyQueueConsumerBuiltinKeys;
import io.openmessaging.joyqueue.domain.JoyQueueNameServerBuiltinKeys;
import io.openmessaging.joyqueue.domain.JoyQueueProducerBuiltinKeys;
import io.openmessaging.joyqueue.domain.JoyQueueTransportBuiltinKeys;
import io.openmessaging.joyqueue.domain.JoyQueueTxFeedbackBuiltinKeys;
import org.joyqueue.client.internal.consumer.config.ConsumerConfig;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.producer.config.ProducerConfig;
import org.joyqueue.client.internal.producer.feedback.config.TxFeedbackConfig;
import org.joyqueue.client.internal.transport.config.TransportConfig;
import org.joyqueue.domain.QosLevel;

/**
 * KeyValueConverter
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public class KeyValueConverter {

    public static NameServerConfig convertNameServerConfig(KeyValue attributes) {
        NameServerConfig nameServerConfig = new NameServerConfig();
        nameServerConfig.setAddress(attributes.getString(JoyQueueNameServerBuiltinKeys.ACCESS_POINTS));
        nameServerConfig.setApp(attributes.getString(JoyQueueNameServerBuiltinKeys.ACCOUNT_ID));
        nameServerConfig.setToken(attributes.getString(JoyQueueNameServerBuiltinKeys.ACCOUNT_KEY));
        nameServerConfig.setRegion(attributes.getString(JoyQueueNameServerBuiltinKeys.REGION));
        nameServerConfig.setNamespace(attributes.getString(JoyQueueNameServerBuiltinKeys.NAMESPACE));
        nameServerConfig.setUpdateMetadataInterval(KeyValueHelper.getInt(attributes, JoyQueueNameServerBuiltinKeys.METADATA_UPDATE_INTERVAL, nameServerConfig.getUpdateMetadataInterval()));
        nameServerConfig.setTempMetadataInterval(KeyValueHelper.getInt(attributes, JoyQueueNameServerBuiltinKeys.METADATA_TEMP_INTERVAL, nameServerConfig.getTempMetadataInterval()));
        nameServerConfig.setUpdateMetadataThread(KeyValueHelper.getInt(attributes, JoyQueueNameServerBuiltinKeys.METADATA_UPDATE_THREAD, nameServerConfig.getUpdateMetadataThread()));
        nameServerConfig.setUpdateMetadataQueueSize(KeyValueHelper.getInt(attributes, JoyQueueNameServerBuiltinKeys.METADATA_UPDATE_QUEUE_SIZE, nameServerConfig.getUpdateMetadataQueueSize()));
        return nameServerConfig;
    }

    public static TransportConfig convertTransportConfig(KeyValue attributes) {
        TransportConfig transportConfig = new TransportConfig();
        transportConfig.setConnections(KeyValueHelper.getInt(attributes, JoyQueueTransportBuiltinKeys.CONNECTIONS, transportConfig.getConnections()));
        transportConfig.setSoTimeout(KeyValueHelper.getInt(attributes, JoyQueueTransportBuiltinKeys.SO_TIMEOUT, transportConfig.getSoTimeout()));
        transportConfig.setIoThreads(KeyValueHelper.getInt(attributes, JoyQueueTransportBuiltinKeys.IO_THREADS, transportConfig.getIoThreads()));
        transportConfig.setCallbackThreads(KeyValueHelper.getInt(attributes, JoyQueueTransportBuiltinKeys.CALLBACK_THREADS, transportConfig.getCallbackThreads()));
        transportConfig.setChannelMaxIdleTime(KeyValueHelper.getInt(attributes, JoyQueueTransportBuiltinKeys.CHANNEL_MAX_IDLE_TIME, transportConfig.getChannelMaxIdleTime()));
        transportConfig.setHeartbeatInterval(KeyValueHelper.getInt(attributes, JoyQueueTransportBuiltinKeys.HEARTBEAT_INTERVAL, transportConfig.getHeartbeatInterval()));
        transportConfig.setHeartbeatTimeout(KeyValueHelper.getInt(attributes, JoyQueueTransportBuiltinKeys.HEARTBEAT_TIMEOUT, transportConfig.getHeartbeatTimeout()));
        transportConfig.setSoLinger(KeyValueHelper.getInt(attributes, JoyQueueTransportBuiltinKeys.SO_LINGER, transportConfig.getSoLinger()));
        transportConfig.setTcpNoDelay(attributes.getBoolean(JoyQueueTransportBuiltinKeys.TCP_NO_DELAY, transportConfig.isTcpNoDelay()));
        transportConfig.setKeepAlive(attributes.getBoolean(JoyQueueTransportBuiltinKeys.KEEPALIVE, transportConfig.isKeepAlive()));
        transportConfig.setSoTimeout(KeyValueHelper.getInt(attributes, JoyQueueTransportBuiltinKeys.SO_TIMEOUT, transportConfig.getSoTimeout()));
        transportConfig.setSendTimeout(KeyValueHelper.getInt(attributes, JoyQueueTransportBuiltinKeys.SEND_TIMEOUT, transportConfig.getSoTimeout()));
        transportConfig.setSocketBufferSize(KeyValueHelper.getInt(attributes, JoyQueueTransportBuiltinKeys.SOCKET_BUFFER_SIZE, transportConfig.getSocketBufferSize()));
        transportConfig.setMaxOneway(KeyValueHelper.getInt(attributes, JoyQueueTransportBuiltinKeys.MAX_ONEWAY, transportConfig.getMaxOneway()));
        transportConfig.setMaxAsync(KeyValueHelper.getInt(attributes, JoyQueueTransportBuiltinKeys.MAX_ASYNC, transportConfig.getMaxAsync()));
        transportConfig.setNonBlockOneway(attributes.getBoolean(JoyQueueTransportBuiltinKeys.NONBLOCK_ONEWAY, transportConfig.isNonBlockOneway()));
        transportConfig.getRetryPolicy().setMaxRetrys(KeyValueHelper.getInt(attributes, JoyQueueTransportBuiltinKeys.RETRIES, transportConfig.getRetryPolicy().getMaxRetrys()));
        return transportConfig;
    }

    public static ProducerConfig convertProducerConfig(KeyValue attributes) {
        ProducerConfig producerConfig = new ProducerConfig();
        producerConfig.setTimeout(attributes.getLong(JoyQueueProducerBuiltinKeys.TIMEOUT, producerConfig.getTimeout()));
        producerConfig.setProduceTimeout(attributes.getLong(JoyQueueProducerBuiltinKeys.PRODUCE_TIMEOUT, producerConfig.getProduceTimeout()));
        producerConfig.setTransactionTimeout(attributes.getLong(JoyQueueProducerBuiltinKeys.TRANSACTION_TIMEOUT, producerConfig.getTransactionTimeout()));
        producerConfig.setFailover(attributes.getBoolean(JoyQueueProducerBuiltinKeys.FAILOVER, producerConfig.isFailover()));
        producerConfig.getRetryPolicy().setMaxRetrys(KeyValueHelper.getInt(attributes, JoyQueueProducerBuiltinKeys.RETRIES, producerConfig.getRetryPolicy().getMaxRetrys()));
        producerConfig.setQosLevel(QosLevel.valueOf(KeyValueHelper.getInt(attributes, JoyQueueProducerBuiltinKeys.QOSLEVEL, producerConfig.getQosLevel().value())));
        producerConfig.setCompress(attributes.getBoolean(JoyQueueProducerBuiltinKeys.COMPRESS, producerConfig.isCompress()));
        producerConfig.setCompressType(KeyValueHelper.getString(attributes, JoyQueueProducerBuiltinKeys.COMPRESS_TYPE, producerConfig.getCompressType()));
        producerConfig.setCompressThreshold(KeyValueHelper.getInt(attributes, JoyQueueProducerBuiltinKeys.COMPRESS_THRESHOLD, producerConfig.getCompressThreshold()));
        producerConfig.setBatch(attributes.getBoolean(JoyQueueProducerBuiltinKeys.BATCH, producerConfig.isBatch()));
        producerConfig.setSelectorType(KeyValueHelper.getString(attributes, JoyQueueProducerBuiltinKeys.SELECTOR_TYPE, producerConfig.getSelectorType()));
        producerConfig.setBusinessIdLengthLimit(KeyValueHelper.getInt(attributes, JoyQueueProducerBuiltinKeys.BUSINESSID_LENGTH_LIMIT, producerConfig.getBusinessIdLengthLimit()));
        producerConfig.setBodyLengthLimit(KeyValueHelper.getInt(attributes, JoyQueueProducerBuiltinKeys.BODY_LENGTH_LIMIT, producerConfig.getBodyLengthLimit()));
        producerConfig.setBatchBodyLengthLimit(KeyValueHelper.getInt(attributes, JoyQueueProducerBuiltinKeys.BATCH_BODY_LENGTH_LIMIT, producerConfig.getBatchBodyLengthLimit()));
        return producerConfig;
    }

    public static ProducerConfig convertProducerConfig(NameServerConfig nameServerConfig, KeyValue attributes) {
        ProducerConfig producerConfig = convertProducerConfig(attributes);
        producerConfig.setApp(nameServerConfig.getApp());
        return producerConfig;
    }

    public static ConsumerConfig convertConsumerConfig(KeyValue attributes) {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setGroup(KeyValueHelper.getString(attributes, JoyQueueConsumerBuiltinKeys.GROUP, consumerConfig.getGroup()));
        consumerConfig.setBatchSize(KeyValueHelper.getInt(attributes, JoyQueueConsumerBuiltinKeys.BATCH_SIZE, consumerConfig.getBatchSize()));
        consumerConfig.setAckTimeout(attributes.getLong(JoyQueueConsumerBuiltinKeys.ACK_TIMEOUT, consumerConfig.getAckTimeout()));
        consumerConfig.setTimeout(attributes.getLong(JoyQueueConsumerBuiltinKeys.TIMEOUT, consumerConfig.getTimeout()));
        consumerConfig.setPollTimeout(attributes.getLong(JoyQueueConsumerBuiltinKeys.POLL_TIMEOUT, consumerConfig.getPollTimeout()));
        consumerConfig.setLongPollTimeout(attributes.getLong(JoyQueueConsumerBuiltinKeys.LONGPOLL_TIMEOUT, consumerConfig.getLongPollTimeout()));
        consumerConfig.setInterval(attributes.getLong(JoyQueueConsumerBuiltinKeys.INTERVAL, consumerConfig.getInterval()));
        consumerConfig.setIdleInterval(attributes.getLong(JoyQueueConsumerBuiltinKeys.IDLE_INTERVAL, consumerConfig.getIdleInterval()));
        consumerConfig.setSessionTimeout(attributes.getLong(JoyQueueConsumerBuiltinKeys.SESSION_TIMEOUT, consumerConfig.getSessionTimeout()));
        consumerConfig.setThread(KeyValueHelper.getInt(attributes, JoyQueueConsumerBuiltinKeys.THREAD, consumerConfig.getThread()));
        consumerConfig.setFailover(attributes.getBoolean(JoyQueueConsumerBuiltinKeys.FAILOVER, consumerConfig.isFailover()));
        consumerConfig.setForceAck(attributes.getBoolean(JoyQueueConsumerBuiltinKeys.FORCE_ACK, consumerConfig.isForceAck()));
        consumerConfig.setLoadBalance(attributes.getBoolean(JoyQueueConsumerBuiltinKeys.LOADBALANCE, consumerConfig.isLoadBalance()));
        consumerConfig.setLoadBalanceType(KeyValueHelper.getString(attributes, JoyQueueConsumerBuiltinKeys.LOADBALANCE_TYPE, consumerConfig.getLoadBalanceType()));
        consumerConfig.setBroadcastGroup(KeyValueHelper.getString(attributes, JoyQueueConsumerBuiltinKeys.BROADCAST_GROUP, consumerConfig.getBroadcastGroup()));
        consumerConfig.setBroadcastLocalPath(KeyValueHelper.getString(attributes, JoyQueueConsumerBuiltinKeys.BROADCAST_LOCAL_PATH, consumerConfig.getBroadcastLocalPath()));
        consumerConfig.setBroadcastPersistInterval(KeyValueHelper.getInt(attributes, JoyQueueConsumerBuiltinKeys.BROADCAST_PERSIST_INTERVAL, consumerConfig.getBroadcastPersistInterval()));
        consumerConfig.setBroadcastIndexExpireTime(KeyValueHelper.getInt(attributes, JoyQueueConsumerBuiltinKeys.BROADCAST_INDEX_EXPIRE_TIME, consumerConfig.getBroadcastIndexExpireTime()));
        consumerConfig.setBroadcastIndexAutoReset(KeyValueHelper.getInt(attributes, JoyQueueConsumerBuiltinKeys.BROADCAST_INDEX_AUTO_RESET, consumerConfig.getBroadcastIndexAutoReset()));
        return consumerConfig;
    }

    public static ConsumerConfig convertConsumerConfig(NameServerConfig nameServerConfig, KeyValue attributes) {
        ConsumerConfig consumerConfig = convertConsumerConfig(attributes);
        consumerConfig.setApp(nameServerConfig.getApp());
        return consumerConfig;
    }

    public static TxFeedbackConfig convertFeedbackConfig(KeyValue attributes) {
        TxFeedbackConfig txFeedbackConfig = new TxFeedbackConfig();
        txFeedbackConfig.setTimeout(attributes.getLong(JoyQueueTxFeedbackBuiltinKeys.TIMEOUT, txFeedbackConfig.getTimeout()));
        txFeedbackConfig.setLongPollTimeout(attributes.getLong(JoyQueueTxFeedbackBuiltinKeys.LONGPOLL_TIMEOUT, txFeedbackConfig.getLongPollTimeout()));
        txFeedbackConfig.setFetchInterval(KeyValueHelper.getInt(attributes, JoyQueueTxFeedbackBuiltinKeys.FETCH_INTERVAL, txFeedbackConfig.getFetchInterval()));
        txFeedbackConfig.setFetchSize(KeyValueHelper.getInt(attributes, JoyQueueTxFeedbackBuiltinKeys.FETCH_SIZE, txFeedbackConfig.getFetchSize()));
        return txFeedbackConfig;
    }

    public static TxFeedbackConfig convertFeedbackConfig(NameServerConfig nameServerConfig, KeyValue attributes) {
        TxFeedbackConfig txFeedbackConfig = convertFeedbackConfig(attributes);
        txFeedbackConfig.setApp(nameServerConfig.getApp());
        return txFeedbackConfig;
    }
}