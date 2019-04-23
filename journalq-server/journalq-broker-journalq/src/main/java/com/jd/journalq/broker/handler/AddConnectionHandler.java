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
package com.jd.journalq.broker.handler;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.config.BrokerConfig;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.JournalqCommandHandler;
import com.jd.journalq.broker.monitor.SessionManager;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.message.SourceType;
import com.jd.journalq.network.command.AddConnection;
import com.jd.journalq.network.command.AddConnectionAck;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.security.Authentication;
import com.jd.journalq.toolkit.network.IpUtil;
import com.jd.journalq.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * AddConnectionHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/29
 */
public class AddConnectionHandler implements JournalqCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(AddConnectionHandler.class);

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
        AddConnection addConnection = (AddConnection) command.getPayload();

        if (!authentication.auth(addConnection.getApp(), addConnection.getToken()).isSuccess()) {
            logger.warn("user auth failed, transport: {}, app: {}", transport, addConnection.getApp());
            return BooleanAck.build(JournalqCode.CN_AUTHENTICATION_ERROR.getCode(),
                    JournalqCode.CN_AUTHENTICATION_ERROR.getMessage() + String.format(", app: %s", addConnection.getApp()));
        }

        Connection connection = buildConnection(transport, addConnection);
        if (sessionManager.addConnection(connection)) {
            // 绑定连接
            SessionHelper.setConnection(transport, connection);
        }

        AddConnectionAck addConnectionAck = new AddConnectionAck();
        addConnectionAck.setConnectionId(connection.getId());
        return new Command(addConnectionAck);
    }

    protected Connection buildConnection(Transport transport, AddConnection addConnection) {
        Connection connection = new Connection();
        connection.setTransport(transport);
        connection.setApp(addConnection.getApp());
        connection.setId(generateConnectionId(transport, addConnection));
        connection.setRegion(addConnection.getRegion());
        connection.setNamespace(addConnection.getNamespace());
        connection.setLanguage(addConnection.getLanguage());
        connection.setSource(SourceType.JMQ.name());
        connection.setCreateTime(SystemClock.now());
        connection.setVersion(addConnection.getClientId().getVersion());
        connection.setAddressStr(IpUtil.toAddress(transport.remoteAddress()));
        connection.setHost(((InetSocketAddress) transport.remoteAddress()).getHostString());
        connection.setAddress(IpUtil.toByte((InetSocketAddress) transport.remoteAddress()));
        connection.setServerAddress(brokerConfig.getFrontendConfig().getHost().getBytes());
        connection.setSystem(authentication.isAdmin(addConnection.getApp()));
        return connection;
    }

    protected String generateConnectionId(Transport transport, AddConnection addConnection) {
        InetSocketAddress inetRemoteAddress = (InetSocketAddress) transport.remoteAddress();
        return String.format("%s-%s_%s-%s-%s",
                addConnection.getClientId().getVersion(), inetRemoteAddress.getHostString(), inetRemoteAddress.getPort(), SystemClock.now(), addConnection.getClientId().getSequence());
    }

    @Override
    public int type() {
        return JournalqCommandType.ADD_CONNECTION.getCode();
    }
}