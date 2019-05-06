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
package com.jd.journalq.broker.kafka.session;


import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.kafka.helper.KafkaClientHelper;
import com.jd.journalq.broker.monitor.SessionManager;
import com.jd.journalq.message.SourceType;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Consumer;
import com.jd.journalq.network.session.Language;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.toolkit.network.IpUtil;
import com.jd.journalq.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * kafka连接管理器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/7/3
 */
public class KafkaConnectionManager {

    protected static final Logger logger = LoggerFactory.getLogger(KafkaConnectionManager.class);

    private SessionManager sessionManager;

    public KafkaConnectionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void addConnection(Transport transport, String clientId, String version) {
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
        connection.setLanguage(Language.JAVA);
        connection.setSource(SourceType.KAFKA.name());
        connection.setTransport(transport);
        connection.setCreateTime(SystemClock.now());
        this.sessionManager.addConnection(connection);
        SessionHelper.setConnection(transport, connection);
    }

    public void addProducer(Transport transport, String topic) {
        Connection connection = SessionHelper.getConnection(transport);
        String app = connection.getApp();
        if (connection.containsProducer(topic, app)) {
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
        if (connection.containsConsumer(topic, app)) {
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
        return String.format("%s-%s_%s-%s",
                connection.getVersion(), inetRemoteAddress.getHostString(), inetRemoteAddress.getPort(), SystemClock.now());
    }

    protected String generateConsumerId(Connection connection, String topic) {
        InetSocketAddress inetRemoteAddress = (InetSocketAddress) connection.getTransport().remoteAddress();
        return String.format("%s-%s_%s-%s",
                connection.getVersion(), inetRemoteAddress.getHostString(), inetRemoteAddress.getPort(), SystemClock.now());
    }

    protected String generateConnectionId(SocketAddress remoteAddress, String clientId, String version) {
        InetSocketAddress inetRemoteAddress = (InetSocketAddress) remoteAddress;
        return String.format("%s-%s_%s-%s",
                version, inetRemoteAddress.getHostString(), inetRemoteAddress.getPort(), SystemClock.now());
    }
}