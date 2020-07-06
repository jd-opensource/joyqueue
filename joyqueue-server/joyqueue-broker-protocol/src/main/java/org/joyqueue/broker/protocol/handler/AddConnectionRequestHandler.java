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
package org.joyqueue.broker.protocol.handler;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.config.BrokerConfig;
import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.broker.monitor.SessionManager;
import org.joyqueue.broker.protocol.JoyQueueCommandHandler;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.message.SourceType;
import org.joyqueue.network.command.AddConnectionRequest;
import org.joyqueue.network.command.AddConnectionResponse;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.security.Authentication;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * AddConnectionRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/11/29
 */
public class AddConnectionRequestHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(AddConnectionRequestHandler.class);

    private BrokerConfig brokerConfig;
    private Authentication authentication;
    private SessionManager sessionManager;
    private ClusterManager clusterManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerConfig = brokerContext.getBrokerConfig();
        this.authentication = brokerContext.getAuthentication();
        this.sessionManager = brokerContext.getSessionManager();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        AddConnectionRequest addConnectionRequest = (AddConnectionRequest) command.getPayload();

        if (!authentication.auth(addConnectionRequest.getApp(), addConnectionRequest.getToken()).isSuccess()) {
            logger.warn("user auth failed, transport: {}, app: {}", transport, addConnectionRequest.getApp());
            return BooleanAck.build(JoyQueueCode.CN_AUTHENTICATION_ERROR.getCode(),
                    JoyQueueCode.CN_AUTHENTICATION_ERROR.getMessage() + String.format(", app: %s", addConnectionRequest.getApp()));
        }

        Connection connection = SessionHelper.getConnection(transport);
        if (connection == null) {
            connection = buildConnection(transport, addConnectionRequest);
            if (SessionHelper.putIfAbsentConnection(transport, connection)) {
                // 绑定连接
                sessionManager.addConnection(connection);
            }
        }

        AddConnectionResponse addConnectionResponse = new AddConnectionResponse();
        addConnectionResponse.setConnectionId(connection.getId());
        return new Command(addConnectionResponse);
    }

    protected Connection buildConnection(Transport transport, AddConnectionRequest addConnectionRequest) {
        Connection connection = new Connection();
        connection.setTransport(transport);
        connection.setApp(addConnectionRequest.getApp());
        connection.setId(generateConnectionId(transport, addConnectionRequest));
        connection.setRegion(addConnectionRequest.getRegion());
        connection.setNamespace(addConnectionRequest.getNamespace());
        connection.setLanguage(addConnectionRequest.getLanguage());
        connection.setSource(SourceType.JOYQUEUE.name());
        connection.setCreateTime(SystemClock.now());
        connection.setVersion(addConnectionRequest.getClientId().getVersion());
        connection.setAddressStr(IpUtil.toAddress(transport.remoteAddress()));
        connection.setHost(((InetSocketAddress) transport.remoteAddress()).getHostString());
        connection.setAddress(IpUtil.toByte((InetSocketAddress) transport.remoteAddress()));
        connection.setServerAddress(brokerConfig.getFrontendConfig().getHost().getBytes());
        connection.setSystem(authentication.isAdmin(addConnectionRequest.getApp()));
        connection.setAuth(true);
        return connection;
    }

    protected String generateConnectionId(Transport transport, AddConnectionRequest addConnectionRequest) {
        InetSocketAddress inetRemoteAddress = (InetSocketAddress) transport.remoteAddress();
        return String.format("%s-%s_%s-%s-%s",
                addConnectionRequest.getClientId().getVersion(), inetRemoteAddress.getHostString(), inetRemoteAddress.getPort(), SystemClock.now(), addConnectionRequest.getClientId().getSequence());
    }

    @Override
    public int type() {
        return JoyQueueCommandType.ADD_CONNECTION_REQUEST.getCode();
    }
}