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
package io.chubao.joyqueue.broker.protocol.handler;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.config.BrokerConfig;
import io.chubao.joyqueue.broker.helper.SessionHelper;
import io.chubao.joyqueue.broker.monitor.SessionManager;
import io.chubao.joyqueue.broker.protocol.JoyQueueCommandHandler;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.message.SourceType;
import io.chubao.joyqueue.network.command.AddConnectionRequest;
import io.chubao.joyqueue.network.command.AddConnectionResponse;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.session.Connection;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.security.Authentication;
import io.chubao.joyqueue.toolkit.network.IpUtil;
import io.chubao.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * AddConnectionRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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

        Connection connection = buildConnection(transport, addConnectionRequest);
        if (sessionManager.addConnection(connection)) {
            // 绑定连接
            SessionHelper.setConnection(transport, connection);
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