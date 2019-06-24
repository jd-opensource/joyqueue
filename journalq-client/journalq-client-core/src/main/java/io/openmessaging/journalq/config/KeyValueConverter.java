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
package io.openmessaging.journalq.config;

import com.jd.journalq.client.internal.consumer.config.ConsumerConfig;
import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.client.internal.producer.config.ProducerConfig;
import com.jd.journalq.client.internal.producer.feedback.config.TxFeedbackConfig;
import com.jd.journalq.client.internal.transport.config.TransportConfig;
import com.jd.journalq.domain.QosLevel;
import io.openmessaging.KeyValue;
import io.openmessaging.journalq.domain.JournalQConsumerBuiltinKeys;
import io.openmessaging.journalq.domain.JournalQNameServerBuiltinKeys;
import io.openmessaging.journalq.domain.JournalQProducerBuiltinKeys;
import io.openmessaging.journalq.domain.JournalQTransportBuiltinKeys;
import io.openmessaging.journalq.domain.JournalQTxFeedbackBuiltinKeys;

/**
 * KeyValueConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public class KeyValueConverter {

    public static NameServerConfig convertNameServerConfig(KeyValue attributes) {
        NameServerConfig nameServerConfig = new NameServerConfig();
        nameServerConfig.setAddress(attributes.getString(JournalQNameServerBuiltinKeys.ACCESS_POINTS));
        nameServerConfig.setApp(attributes.getString(JournalQNameServerBuiltinKeys.ACCOUNT_ID));
        nameServerConfig.setToken(attributes.getString(JournalQNameServerBuiltinKeys.ACCOUNT_KEY));
        nameServerConfig.setRegion(attributes.getString(JournalQNameServerBuiltinKeys.REGION));
        nameServerConfig.setNamespace(attributes.getString(JournalQNameServerBuiltinKeys.NAMESPACE));
        nameServerConfig.setUpdateMetadataInterval(KeyValueHelper.getInt(attributes, JournalQNameServerBuiltinKeys.METADATA_UPDATE_INTERVAL, nameServerConfig.getUpdateMetadataInterval()));
        nameServerConfig.setTempMetadataInterval(KeyValueHelper.getInt(attributes, JournalQNameServerBuiltinKeys.METADATA_TEMP_INTERVAL, nameServerConfig.getTempMetadataInterval()));
        nameServerConfig.setUpdateMetadataThread(KeyValueHelper.getInt(attributes, JournalQNameServerBuiltinKeys.METADATA_UPDATE_THREAD, nameServerConfig.getUpdateMetadataThread()));
        nameServerConfig.setUpdateMetadataQueueSize(KeyValueHelper.getInt(attributes, JournalQNameServerBuiltinKeys.METADATA_UPDATE_QUEUE_SIZE, nameServerConfig.getUpdateMetadataQueueSize()));
        return nameServerConfig;
    }

    public static TransportConfig convertTransportConfig(KeyValue attributes) {
        TransportConfig transportConfig = new TransportConfig();
        transportConfig.setConnections(KeyValueHelper.getInt(attributes, JournalQTransportBuiltinKeys.CONNECTIONS, transportConfig.getConnections()));
        transportConfig.setSoTimeout(KeyValueHelper.getInt(attributes, JournalQTransportBuiltinKeys.SO_TIMEOUT, transportConfig.getSoTimeout()));
        transportConfig.setIoThreads(KeyValueHelper.getInt(attributes, JournalQTransportBuiltinKeys.IO_THREADS, transportConfig.getIoThreads()));
        transportConfig.setCallbackThreads(KeyValueHelper.getInt(attributes, JournalQTransportBuiltinKeys.CALLBACK_THREADS, transportConfig.getCallbackThreads()));
        transportConfig.setChannelMaxIdleTime(KeyValueHelper.getInt(attributes, JournalQTransportBuiltinKeys.CHANNEL_MAX_IDLE_TIME, transportConfig.getChannelMaxIdleTime()));
        transportConfig.setHeartbeatInterval(KeyValueHelper.getInt(attributes, JournalQTransportBuiltinKeys.HEARTBEAT_INTERVAL, transportConfig.getHeartbeatInterval()));
        transportConfig.setHeartbeatTimeout(KeyValueHelper.getInt(attributes, JournalQTransportBuiltinKeys.HEARTBEAT_TIMEOUT, transportConfig.getHeartbeatTimeout()));
        transportConfig.setSoLinger(KeyValueHelper.getInt(attributes, JournalQTransportBuiltinKeys.SO_LINGER, transportConfig.getSoLinger()));
        transportConfig.setTcpNoDelay(attributes.getBoolean(JournalQTransportBuiltinKeys.CONNECTIONS, transportConfig.isTcpNoDelay()));
        transportConfig.setKeepAlive(attributes.getBoolean(JournalQTransportBuiltinKeys.KEEPALIVE, transportConfig.isKeepAlive()));
        transportConfig.setSoTimeout(KeyValueHelper.getInt(attributes, JournalQTransportBuiltinKeys.SO_TIMEOUT, transportConfig.getSoTimeout()));
        transportConfig.setSocketBufferSize(KeyValueHelper.getInt(attributes, JournalQTransportBuiltinKeys.SOCKET_BUFFER_SIZE, transportConfig.getSocketBufferSize()));
        transportConfig.setMaxOneway(KeyValueHelper.getInt(attributes, JournalQTransportBuiltinKeys.MAX_ONEWAY, transportConfig.getMaxOneway()));
        transportConfig.setMaxAsync(KeyValueHelper.getInt(attributes, JournalQTransportBuiltinKeys.MAX_ASYNC, transportConfig.getMaxAsync()));
        transportConfig.setNonBlockOneway(attributes.getBoolean(JournalQTransportBuiltinKeys.NONBLOCK_ONEWAY, transportConfig.isNonBlockOneway()));
        transportConfig.getRetryPolicy().setMaxRetrys(KeyValueHelper.getInt(attributes, JournalQTransportBuiltinKeys.RETRIES, transportConfig.getRetryPolicy().getMaxRetrys()));
        return transportConfig;
    }

    public static ProducerConfig convertProducerConfig(KeyValue attributes) {
        ProducerConfig producerConfig = new ProducerConfig();
        producerConfig.setTimeout(attributes.getLong(JournalQProducerBuiltinKeys.TIMEOUT, producerConfig.getTimeout()));
        producerConfig.setProduceTimeout(attributes.getLong(JournalQProducerBuiltinKeys.PRODUCE_TIMEOUT, producerConfig.getProduceTimeout()));
        producerConfig.setTransactionTimeout(attributes.getLong(JournalQProducerBuiltinKeys.TRANSACTION_TIMEOUT, producerConfig.getTransactionTimeout()));
        producerConfig.setFailover(attributes.getBoolean(JournalQProducerBuiltinKeys.FAILOVER, producerConfig.isFailover()));
        producerConfig.getRetryPolicy().setMaxRetrys(KeyValueHelper.getInt(attributes, JournalQProducerBuiltinKeys.RETRIES, producerConfig.getRetryPolicy().getMaxRetrys()));
        producerConfig.setQosLevel(QosLevel.valueOf(KeyValueHelper.getInt(attributes, JournalQProducerBuiltinKeys.QOSLEVEL, producerConfig.getQosLevel().value())));
        producerConfig.setCompress(attributes.getBoolean(JournalQProducerBuiltinKeys.COMPRESS, producerConfig.isCompress()));
        producerConfig.setCompressType(KeyValueHelper.getString(attributes, JournalQProducerBuiltinKeys.COMPRESS_TYPE, producerConfig.getCompressType()));
        producerConfig.setCompressThreshold(KeyValueHelper.getInt(attributes, JournalQProducerBuiltinKeys.COMPRESS_THRESHOLD, producerConfig.getCompressThreshold()));
        producerConfig.setBatch(attributes.getBoolean(JournalQProducerBuiltinKeys.BATCH, producerConfig.isBatch()));
        producerConfig.setSelectorType(KeyValueHelper.getString(attributes, JournalQProducerBuiltinKeys.SELECTOR_TYPE, producerConfig.getSelectorType()));
        producerConfig.setBusinessIdLengthLimit(KeyValueHelper.getInt(attributes, JournalQProducerBuiltinKeys.BUSINESSID_LENGTH_LIMIT, producerConfig.getBusinessIdLengthLimit()));
        producerConfig.setBodyLengthLimit(KeyValueHelper.getInt(attributes, JournalQProducerBuiltinKeys.BODY_LENGTH_LIMIT, producerConfig.getBodyLengthLimit()));
        producerConfig.setBatchBodyLengthLimit(KeyValueHelper.getInt(attributes, JournalQProducerBuiltinKeys.BATCH_BODY_LENGTH_LIMIT, producerConfig.getBatchBodyLengthLimit()));
        return producerConfig;
    }

    public static ProducerConfig convertProducerConfig(NameServerConfig nameServerConfig, KeyValue attributes) {
        ProducerConfig producerConfig = convertProducerConfig(attributes);
        producerConfig.setApp(nameServerConfig.getApp());
        return producerConfig;
    }

    public static ConsumerConfig convertConsumerConfig(KeyValue attributes) {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setGroup(KeyValueHelper.getString(attributes, JournalQConsumerBuiltinKeys.GROUP, consumerConfig.getGroup()));
        consumerConfig.setBatchSize(KeyValueHelper.getInt(attributes, JournalQConsumerBuiltinKeys.BATCH_SIZE, consumerConfig.getBatchSize()));
        consumerConfig.setAckTimeout(attributes.getLong(JournalQConsumerBuiltinKeys.ACK_TIMEOUT, consumerConfig.getAckTimeout()));
        consumerConfig.setTimeout(attributes.getLong(JournalQConsumerBuiltinKeys.TIMEOUT, consumerConfig.getTimeout()));
        consumerConfig.setPollTimeout(attributes.getLong(JournalQConsumerBuiltinKeys.POLL_TIMEOUT, consumerConfig.getPollTimeout()));
        consumerConfig.setLongPollTimeout(attributes.getLong(JournalQConsumerBuiltinKeys.LONGPOLL_TIMEOUT, consumerConfig.getLongPollTimeout()));
        consumerConfig.setInterval(attributes.getLong(JournalQConsumerBuiltinKeys.INTERVAL, consumerConfig.getInterval()));
        consumerConfig.setIdleInterval(attributes.getLong(JournalQConsumerBuiltinKeys.IDLE_INTERVAL, consumerConfig.getIdleInterval()));
        consumerConfig.setSessionTimeout(attributes.getLong(JournalQConsumerBuiltinKeys.SESSION_TIMEOUT, consumerConfig.getSessionTimeout()));
        consumerConfig.setThread(KeyValueHelper.getInt(attributes, JournalQConsumerBuiltinKeys.THREAD, consumerConfig.getThread()));
        consumerConfig.setFailover(attributes.getBoolean(JournalQConsumerBuiltinKeys.FAILOVER, consumerConfig.isFailover()));
        consumerConfig.setLoadBalance(attributes.getBoolean(JournalQConsumerBuiltinKeys.LOADBALANCE, consumerConfig.isLoadBalance()));
        consumerConfig.setLoadBalanceType(KeyValueHelper.getString(attributes, JournalQConsumerBuiltinKeys.LOADBALANCE_TYPE, consumerConfig.getLoadBalanceType()));
        consumerConfig.setBroadcastGroup(KeyValueHelper.getString(attributes, JournalQConsumerBuiltinKeys.BROADCAST_GROUP, consumerConfig.getBroadcastGroup()));
        consumerConfig.setBroadcastLocalPath(KeyValueHelper.getString(attributes, JournalQConsumerBuiltinKeys.BROADCAST_LOCAL_PATH, consumerConfig.getBroadcastLocalPath()));
        consumerConfig.setBroadcastPersistInterval(KeyValueHelper.getInt(attributes, JournalQConsumerBuiltinKeys.BROADCAST_PERSIST_INTERVAL, consumerConfig.getBroadcastPersistInterval()));
        consumerConfig.setBroadcastIndexExpireTime(KeyValueHelper.getInt(attributes, JournalQConsumerBuiltinKeys.BROADCAST_INDEX_EXPIRE_TIME, consumerConfig.getBroadcastIndexExpireTime()));
        return consumerConfig;
    }

    public static ConsumerConfig convertConsumerConfig(NameServerConfig nameServerConfig, KeyValue attributes) {
        ConsumerConfig consumerConfig = convertConsumerConfig(attributes);
        consumerConfig.setApp(nameServerConfig.getApp());
        return consumerConfig;
    }

    public static TxFeedbackConfig convertFeedbackConfig(KeyValue attributes) {
        TxFeedbackConfig txFeedbackConfig = new TxFeedbackConfig();
        txFeedbackConfig.setTimeout(attributes.getLong(JournalQTxFeedbackBuiltinKeys.TIMEOUT, txFeedbackConfig.getTimeout()));
        txFeedbackConfig.setLongPollTimeout(attributes.getLong(JournalQTxFeedbackBuiltinKeys.LONGPOLL_TIMEOUT, txFeedbackConfig.getLongPollTimeout()));
        txFeedbackConfig.setFetchInterval(KeyValueHelper.getInt(attributes, JournalQTxFeedbackBuiltinKeys.FETCH_INTERVAL, txFeedbackConfig.getFetchInterval()));
        txFeedbackConfig.setFetchSize(KeyValueHelper.getInt(attributes, JournalQTxFeedbackBuiltinKeys.FETCH_SIZE, txFeedbackConfig.getFetchSize()));
        return txFeedbackConfig;
    }

    public static TxFeedbackConfig convertFeedbackConfig(NameServerConfig nameServerConfig, KeyValue attributes) {
        TxFeedbackConfig txFeedbackConfig = convertFeedbackConfig(attributes);
        txFeedbackConfig.setApp(nameServerConfig.getApp());
        return txFeedbackConfig;
    }
}