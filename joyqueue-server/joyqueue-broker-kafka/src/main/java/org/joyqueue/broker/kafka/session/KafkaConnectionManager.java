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
package org.joyqueue.broker.kafka.session;


import org.apache.commons.lang3.StringUtils;
import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.broker.kafka.helper.KafkaClientHelper;
import org.joyqueue.broker.monitor.SessionManager;
import org.joyqueue.message.SourceType;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.network.session.Language;
import org.joyqueue.network.session.Producer;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * kafka连接管理器
 *
 * author: gaohaoxiang
 * date: 2018/7/3
 */
public class KafkaConnectionManager {

    protected static final Logger logger = LoggerFactory.getLogger(KafkaConnectionManager.class);

    private SessionManager sessionManager;

    public KafkaConnectionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void addConnection(Transport transport, String clientId, String version) {
        addConnection(transport, clientId, version, Language.JAVA);
    }

    public void addConnection(Transport transport, String clientId, String version, Language language) {
        Connection connection = SessionHelper.getConnection(transport);
        if (connection != null) {
            return;
        }

        clientId = KafkaClientHelper.parseClient(clientId);
        InetSocketAddress remoteAddress = (InetSocketAddress) transport.remoteAddress();
        String id = this.generateConnectionId(remoteAddress, clientId, version);

        connection = new Connection();
        connection.setId(id);
        connection.setApp(clientId);
        connection.setVersion(version);
        connection.setAddress(IpUtil.toByte(remoteAddress));
        connection.setAddressStr(IpUtil.toAddress(remoteAddress));
        connection.setHost(((InetSocketAddress) transport.remoteAddress()).getHostString());
        connection.setLanguage(language);
        connection.setSource(SourceType.KAFKA.name());
        connection.setTransport(transport);
        connection.setCreateTime(SystemClock.now());
        if (this.sessionManager.addConnection(connection)) {
            SessionHelper.putIfAbsentConnection(transport, connection);
        }
    }

    public void addProducer(Transport transport, String topic) {
        Connection connection = SessionHelper.getConnection(transport);
        String app = connection.getApp();
        String producerId = connection.getProducer(topic, app);
        if (StringUtils.isNotBlank(producerId)) {
            return;
        }
        String id = generateProducerId(connection, topic);
        Producer producer = new Producer();
        producer.setId(id);
        producer.setConnectionId(connection.getId());
        producer.setApp(app);
        producer.setTopic(topic);
        producer.setType(Producer.ProducerType.KAFKA);
        this.sessionManager.addProducer(producer);
    }

    public void addConsumer(Transport transport, String topic) {
        Connection connection = SessionHelper.getConnection(transport);
        String app = connection.getApp();
        String consumerId = connection.getConsumer(topic, app);
        if (StringUtils.isNotBlank(consumerId)) {
            return;
        }
        String id = generateConsumerId(connection, topic);
        Consumer consumer = new Consumer();
        consumer.setId(id);
        consumer.setConnectionId(connection.getId());
        consumer.setApp(app);
        consumer.setTopic(topic);
        consumer.setType(Consumer.ConsumeType.KAFKA);
        this.sessionManager.addConsumer(consumer);
    }

    public void addGroup(Transport transport, String group) {
        Connection connection = SessionHelper.getConnection(transport);
        if (connection == null) {
            return;
        }
        String app = connection.getApp();
        connection.setApp(app);
    }

    protected String generateProducerId(Connection connection, String topic) {
        InetSocketAddress inetRemoteAddress = (InetSocketAddress) connection.getTransport().remoteAddress();
        return String.format("%s-%s_%s_%s-%s",
                connection.getVersion(), inetRemoteAddress.getHostString(), topic, inetRemoteAddress.getPort(), SystemClock.now());
    }

    protected String generateConsumerId(Connection connection, String topic) {
        InetSocketAddress inetRemoteAddress = (InetSocketAddress) connection.getTransport().remoteAddress();
        return String.format("%s-%s_%s_%s-%s",
                connection.getVersion(), inetRemoteAddress.getHostString(), topic, inetRemoteAddress.getPort(), SystemClock.now());
    }

    protected String generateConnectionId(SocketAddress remoteAddress, String clientId, String version) {
        InetSocketAddress inetRemoteAddress = (InetSocketAddress) remoteAddress;
        return String.format("%s-%s_%s_%s-%s",
                version, inetRemoteAddress.getHostString(), inetRemoteAddress.getPort(), clientId, SystemClock.now());
    }
}