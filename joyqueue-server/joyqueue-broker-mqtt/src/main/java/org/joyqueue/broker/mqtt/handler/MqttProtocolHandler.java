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
package org.joyqueue.broker.mqtt.handler;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.mqtt.cluster.MqttConnectionManager;
import org.joyqueue.broker.mqtt.cluster.MqttConsumerManager;
import org.joyqueue.broker.mqtt.cluster.MqttProducerManager;
import org.joyqueue.broker.mqtt.cluster.MqttSessionManager;
import org.joyqueue.broker.mqtt.cluster.MqttSubscriptionManager;
import org.joyqueue.broker.mqtt.connection.MqttConnection;
import org.joyqueue.broker.mqtt.message.WillMessage;
import org.joyqueue.broker.mqtt.publish.MessagePublisher;
import org.joyqueue.broker.mqtt.session.MqttSession;
import org.joyqueue.broker.mqtt.subscriptions.MqttSubscription;
import org.joyqueue.broker.mqtt.subscriptions.TopicFilter;
import org.joyqueue.broker.mqtt.util.NettyAttrManager;
import org.joyqueue.domain.AppToken;
import org.joyqueue.network.session.Producer;
import org.joyqueue.nsr.NameService;
import com.google.common.base.Strings;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.service.Service;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.netty.channel.ChannelFutureListener.CLOSE;
import static io.netty.channel.ChannelFutureListener.CLOSE_ON_FAILURE;

/**
 * @author majun8
 */
public class MqttProtocolHandler extends Service {
    private static final Logger LOG = LoggerFactory.getLogger(MqttProtocolHandler.class);

    private ConcurrentMap<String, WillMessage> willStore = new ConcurrentHashMap<>();

    private MqttConnectionManager connectionManager;
    private MqttSessionManager sessionManager;
    private MqttProducerManager producerManager;
    private MqttConsumerManager consumerManager;
    private MqttSubscriptionManager subscriptionManager;
    private MessagePublisher messagePublisher;
    private NameService nameService;

    public MqttProtocolHandler(BrokerContext brokerContext) {
        connectionManager = new MqttConnectionManager();
        messagePublisher = new MessagePublisher(brokerContext, connectionManager);
        sessionManager = new MqttSessionManager(brokerContext, connectionManager);
        producerManager = new MqttProducerManager(connectionManager);
        consumerManager = new MqttConsumerManager(brokerContext, connectionManager, sessionManager, messagePublisher);
        subscriptionManager = new MqttSubscriptionManager(brokerContext);
        nameService = brokerContext.getNameService();
    }

    public MqttConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public MqttSessionManager getSessionManager() {
        return sessionManager;
    }

    public MqttConsumerManager getConsumerManager() {
        return consumerManager;
    }

    @Override
    public void start() throws Exception {
        super.start();
        connectionManager.start();
        sessionManager.start();
        producerManager.start();
        consumerManager.start();
        subscriptionManager.start();
    }

    @Override
    public void stop() {
        super.stop();
        connectionManager.stop();
        sessionManager.stop();
        producerManager.stop();
        consumerManager.stop();
        subscriptionManager.stop();
    }

    public void processConnect(Channel client, MqttConnectMessage connectMessage) {
        String clientId = connectMessage.payload().clientIdentifier();
        boolean isCleanSession = connectMessage.variableHeader().isCleanSession();

        //验证版本
        if (!connectMessage.variableHeader().name().equals("MQTT") ||
                connectMessage.variableHeader().version() != MqttVersion.MQTT_3_1_1.protocolLevel()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("CONN clientID: <{}>, 版本不对断开连接: <{}>", clientId, connectMessage.toString());
            }
            sendAckToClient(client, connectMessage, MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION, false);
            return;
        }

        MqttConnectReturnCode resultCode = checkAuth(connectMessage);
        if (!(resultCode == MqttConnectReturnCode.CONNECTION_ACCEPTED) ||
                Strings.isNullOrEmpty(clientId)) {
            sendAckToClient(client, connectMessage, resultCode, false);
            return;
        }
        addConnection(client, connectMessage, clientId);

        //处理心跳包时间，把心跳包时长和一些其他属性都添加到会话中，方便以后使用
        initializeKeepAliveTimeout(client, connectMessage, clientId);
        storeWillMessage(clientId, connectMessage);

        sessionManager.addSession(clientId, isCleanSession);

        MqttConnAckMessage okResp = sendAckToClient(client, connectMessage, MqttConnectReturnCode.CONNECTION_ACCEPTED, !isCleanSession);

        if (okResp.variableHeader().connectReturnCode().byteValue() != MqttConnectReturnCode.CONNECTION_ACCEPTED.byteValue()) {
            LOG.info("CONNECT-none-accepted clientID: <{}>, ConnectionStatus: <{}>, client-address: <{}>, server-address: <{}>",
                    clientId,
                    okResp.variableHeader().connectReturnCode().byteValue(),
                    client.remoteAddress(),
                    client.localAddress()
            );
        }

        consumerManager.fireConsume(clientId);

        LOG.info("CONNECT successful, clientID: {}, client-address: <{}>, server-address: <{}>", clientId, client.remoteAddress(), client.localAddress());
    }

    private MqttConnAckMessage sendAckToClient(Channel client, MqttConnectMessage connectMessage, MqttConnectReturnCode ackCode, boolean sessionPresent) {

        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, connectMessage.fixedHeader().qosLevel(), false, 0);
        MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                mqttFixedHeader,
                new MqttConnAckVariableHeader(ackCode, sessionPresent),
                null);
        client.writeAndFlush(connAckMessage);
        return connAckMessage;
    }

    private boolean auth(String user, String password) {
        if (Strings.isNullOrEmpty(user) || Strings.isNullOrEmpty(password)) {
            return false;
        } else {
            Date now = Calendar.getInstance().getTime();
            AppToken appToken = nameService.getAppToken(user, password);
            return null != appToken && appToken.getEffectiveTime().before(now) && appToken.getExpirationTime().after(now);
        }
    }

    private MqttConnectReturnCode checkAuth(MqttConnectMessage message) {

        boolean cleanSession = message.variableHeader().isCleanSession();
        String clientID = message.payload().clientIdentifier();
        boolean noId = Strings.isNullOrEmpty(clientID);

        if (noId) {
            LOG.debug("NULL clientID, cleanSession: {}", cleanSession);
            return MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
        }

        if (LOG.isDebugEnabled()){
            LOG.debug("hasUserName: {}", message.variableHeader().hasUserName());
            LOG.debug("hasPassword: {}", message.variableHeader().hasPassword());
        }
        if (message.variableHeader().hasUserName() && message.variableHeader().hasPassword()) {
            String userName = message.payload().userName();
            String passWord = message.payload().password();
            if (LOG.isDebugEnabled()){
                LOG.debug("CONN username: {}, password: {}", userName, passWord);
            }
            if (auth(userName, passWord)) {
                return MqttConnectReturnCode.CONNECTION_ACCEPTED;
            } else {
                return MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD;
            }
        }
        return MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED;
    }

    private void initializeKeepAliveTimeout(Channel client, MqttConnectMessage msg, String clientId) {
        int keepAlive = msg.variableHeader().keepAliveTimeSeconds();
        NettyAttrManager.setAttrKeepAlive(client, keepAlive);
        NettyAttrManager.setAttrClientId(client, clientId);
        NettyAttrManager.setAttrCleanSession(client, msg.variableHeader().isCleanSession());
        int idleTime = Math.round(keepAlive * 1.5f);
        if (client.pipeline().names().contains("idleStateHandler")) {
            client.pipeline().remove("idleStateHandler");
        }
        client.pipeline().addFirst("idleStateHandler", new IdleStateHandler(idleTime, 0, 0));
    }

    private void storeWillMessage(String clientID, MqttConnectMessage msg) {
        if (msg.variableHeader().isWillFlag()) {
            MqttQoS willQos = MqttQoS.valueOf(msg.variableHeader().willQos());
            byte[] willPayload = msg.payload().willMessageInBytes();
            ByteBuffer bb = (ByteBuffer) ByteBuffer.allocate(willPayload.length).put(willPayload).flip();
            WillMessage will = new WillMessage(msg.payload().willTopic(), bb, msg.variableHeader().isWillRetain(), willQos);
            willStore.put(clientID, will);
            LOG.info("Latest will message stored for client: <{}>", clientID);
        }
    }

    private void addConnection(final Channel client, MqttConnectMessage connectMessage, String clientID) {
        String userName = "";
        String passWord = "";
        if (connectMessage.variableHeader().hasUserName() && connectMessage.variableHeader().hasPassword()) {
            userName = connectMessage.payload().userName();
            passWord = connectMessage.payload().password();
        }
        MqttConnection connection = new MqttConnection(
                clientID,
                userName,
                passWord,
                connectMessage.variableHeader().isCleanSession(),
                connectMessage.variableHeader().version(),
                connectMessage.variableHeader().isWillRetain(),
                connectMessage.variableHeader().willQos(),
                connectMessage.variableHeader().isWillFlag(),
                connectMessage.variableHeader().keepAliveTimeSeconds(),
                client);
        connection.setAddress(IpUtil.toByte((InetSocketAddress) client.remoteAddress()));
        connection.setServerAddress(IpUtil.toByte((InetSocketAddress) client.localAddress()));
        final MqttConnection existing = connectionManager.addConnection(connection);
        if (existing != null) {
            //ClientId重复
            LOG.warn("重复clientID的connection连接: <{}>, 需要断开或者重置. 新建的client连接: <{}>", existing, connection);
            existing.getChannel().close().addListener(CLOSE_ON_FAILURE);
            connectionManager.removeConnection(existing);
            connectionManager.addConnection(connection);
        }
    }

    public void processDisconnect(Channel client) {
        String clientID = NettyAttrManager.getAttrClientId(client);
        cleanWillMessage(clientID);

        client.flush();

        client.close().addListener(CLOSE);

        LOG.info("Disconnect successful, clientID: {}", clientID);
    }

    private void cleanWillMessage(String clientID) {
        willStore.remove(clientID);
    }

    public void processPublish(Channel client, MqttPublishMessage publishMessage) {
        String clientID = NettyAttrManager.getAttrClientId(client);
        if (Strings.isNullOrEmpty(clientID)) {
            LOG.error("ClientID is null or empty for publish, aborting... publishEvent message trace: <{}>", publishMessage.toString());
            client.close().addListener(CLOSE_ON_FAILURE);
            return;
        }

        try {
            final String topic = publishMessage.variableHeader().topicName();

            MqttConnection connection = connectionManager.getConnection(clientID);
            if (connection == null) {
                LOG.error("Client connection is null for publish, clientID: {}", clientID);
                client.close().addListener(CLOSE_ON_FAILURE);
                return;
            }
            String application = connection.getApplication();
            if (Strings.isNullOrEmpty(application)) {
                LOG.error("Client application is null for publish, clientID: {}", clientID);
                client.close().addListener(CLOSE_ON_FAILURE);
                return;
            }

            Producer producer = producerManager.getProducer(clientID, application, topic);
            if (producer != null) {
                messagePublisher.publishMessage(producer, client, publishMessage);
            } else {
                throw new Exception("MessageProducer instance null, please check producer create & start...");
            }
        } catch (Throwable th) {
            LOG.error("process Public Message Error!", th);
            client.close().addListener(CLOSE_ON_FAILURE);
        }
    }

    public void processPubAck(Channel client, MqttPubAckMessage pubAckMessage) {
        MqttMessageIdVariableHeader pubAckVariableMessage = pubAckMessage.variableHeader();
        short packageId = (short) pubAckVariableMessage.messageId();
        String clientId = NettyAttrManager.getAttrClientId(client);

        consumerManager.acknowledge(clientId, packageId);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Received PubAck packageID: {}" + packageId);
        }
    }

    public void processSubscribe(Channel client, MqttSubscribeMessage subscribeMessage) {
        List<Integer> resultCodes = new ArrayList<>();
        String clientID = NettyAttrManager.getAttrClientId(client);
        if (connectionManager.isConnected(clientID)) {
            MqttConnection connection = connectionManager.getConnection(clientID);

            if (LOG.isDebugEnabled()) {
                LOG.debug("处理subscribe数据包, clientID: {}, cleanSession: {}", clientID, connection.isCleanSession());
            }

            List<MqttTopicSubscription> topicSubscribes = subscribeMessage.payload().topicSubscriptions();
            LOG.info("Subscribe topics: {}, clientID: {}", topicSubscribes, clientID);

            try {
                if (null != topicSubscribes) {
                    Set<MqttSubscription> topicFilters = subscribe(topicSubscribes, clientID, connection.getClientGroupName(), resultCodes);
                    MqttSession session = sessionManager.getSession(clientID);
                    if (session != null) {
                        for (MqttSubscription subscription : topicFilters) {
                            session.addSubscription(subscription);
                        }
                    }
                } else {
                    // The payload of a SUBSCRIBE packet MUST contain at least one Topic Filter / QoS pair. A SUBSCRIBE packet with no payload is a protocol violation
                    // it MUST close the Network Connection on which it received that Control Packet which caused the protocol violation
                    consumerManager.stopConsume(clientID);
                    sessionManager.removeSession(clientID);
                    connection.getChannel().close().addListener(CLOSE_ON_FAILURE);
                    connectionManager.removeConnection(connection);
                    client.close().addListener(CLOSE_ON_FAILURE);
                }
            } catch (Exception e) {
                LOG.error("subscribe is error!");
                if (resultCodes.size() < topicSubscribes.size()) {
                    int minus = topicSubscribes.size() - resultCodes.size();
                    for (; minus > 0; minus--) {
                        resultCodes.add(MqttQoS.FAILURE.value());
                    }
                }
            }
        }

        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttSubAckMessage subAckMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                mqttFixedHeader,
                MqttMessageIdVariableHeader.from(subscribeMessage.variableHeader().messageId()),
                new MqttSubAckPayload(resultCodes));
        LOG.info("SUBSCRIBE successful, subscribe result: {}", resultCodes);
        client.writeAndFlush(subAckMessage);
    }

    private Set<MqttSubscription> subscribe(List<MqttTopicSubscription> topicSubscribes, String clientID, String clientGroup, List<Integer> resultCodes) throws Exception {
        List<MqttSubscription> needSubsTopicFilters = new ArrayList<>();
        for (MqttTopicSubscription mts : topicSubscribes) {
            // 验证topicfilter是不是合法
            if (!new TopicFilter(mts.topicName()).isValid() /*|| Token.MULTI.toString().equals(mts.topicName())*/) {
                resultCodes.add(MqttQoS.FAILURE.value());
                LOG.warn("topic filter[{}] of clientID[{}] is invalid", mts.topicName(), clientID);
            } else {
                // todo: 目前只支持qos=1的订阅 所以正确返回码统一填充AT_LEAST_ONCE 不填充订阅要求的qos=mts.qualityOfService().value()值 后续实现订阅qos等级要求 先记录qos即可
                needSubsTopicFilters.add(new MqttSubscription(clientID, new TopicFilter(mts.topicName()), mts.qualityOfService()));
                resultCodes.add(MqttQoS.AT_LEAST_ONCE.value());
            }
        }
        LOG.info("Do subscribe topics: {}, clientGroup: {}", needSubsTopicFilters, clientGroup);
        return subscriptionManager.subscribes(clientGroup, needSubsTopicFilters);
    }

    public void processUnsubscribe(Channel client, MqttUnsubscribeMessage unSubscribeMessage) {
        String clientID = NettyAttrManager.getAttrClientId(client);

        int packageId = unSubscribeMessage.variableHeader().messageId();
        MqttQoS qoS = unSubscribeMessage.fixedHeader().qosLevel();

        if (LOG.isDebugEnabled()) {
            LOG.debug("处理unSubscribe数据包, clientID: {}, packageId: {}, Qos: {}", clientID, packageId, qoS);
        }

        if (connectionManager.isConnected(clientID)) {
            MqttConnection connection = connectionManager.getConnection(clientID);

            List<String> topicFilters = unSubscribeMessage.payload().topics();
            LOG.info("UnSubscribe topics: {}", topicFilters);

            try {
                if (topicFilters != null) {
                    Set<MqttSubscription> unSubcriptions = unSubscribe(topicFilters, clientID, connection.getClientGroupName());
                    MqttSession session = sessionManager.getSession(clientID);
                    if (session != null) {
                        for (MqttSubscription subscription : unSubcriptions) {
                            session.removeSubscription(subscription);
                        }
                    }
                } else {
                    // The Payload of an UNSUBSCRIBE packet MUST contain at least one Topic Filter. An UNSUBSCRIBE packet with no payload is a protocol violation
                    // it MUST close the Network Connection on which it received that Control Packet which caused the protocol violation
                    consumerManager.stopConsume(clientID);
                    sessionManager.removeSession(clientID);
                    connection.getChannel().close().addListener(CLOSE_ON_FAILURE);
                    connectionManager.removeConnection(connection);
                    client.close().addListener(CLOSE_ON_FAILURE);
                }
            } catch (Exception e) {
                // ignore
                LOG.error("unSubscribe is error!");
            }
        }
        sendUnSubAck(client, packageId, qoS);
    }

    private void sendUnSubAck(Channel client, int packageID, MqttQoS qoS) {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBACK, false, qoS, false, 0);
        MqttMessage unSubAckMessage = MqttMessageFactory.newMessage(
                mqttFixedHeader,
                MqttMessageIdVariableHeader.from(packageID),
                null);
        LOG.info("UNSUBSCRIBE successful, packageID: {}", packageID);
        client.writeAndFlush(unSubAckMessage);
    }

    private Set<MqttSubscription> unSubscribe(List<String> topicFilters, String clientID, String clientGroup) throws Exception {
        Set<MqttSubscription> needUnSubscriptions = new HashSet<>(topicFilters.size());
        // 发现topic是否合法或则是否已经存在
        for (String topicFilter : topicFilters) {
            // 验证topicfilter是不是合法
            if (!TopicFilter.isValid(topicFilter)) {
                LOG.warn("topic filter[{}] of clientID[{}] is invalid", topicFilter, clientID);
                continue;
            }
            MqttSession session = sessionManager.getSession(clientID);
            if (session != null) {
                Set<MqttSubscription> subscriptions = session.listSubsciptions();
                for (MqttSubscription subscription : subscriptions) {
                    if (subscription.getTopicFilter().toString().equals(topicFilter) ||
                            subscription.getTopicFilter().match(new TopicFilter(topicFilter))) {
                        needUnSubscriptions.add(subscription);
                    }
                }
            }
        }
        if (needUnSubscriptions.isEmpty()) {
            LOG.warn("topic filter for client: <{}> may be null, the topicFilters is <{}>", clientID, topicFilters);
            return new HashSet<>();
        }

        subscriptionManager.unSubscribe(clientGroup, needUnSubscriptions);
        return needUnSubscriptions;
    }
}
