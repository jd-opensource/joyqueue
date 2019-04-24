package com.jd.journalq.broker.protocol.handler;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.config.BrokerConfig;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.monitor.SessionManager;
import com.jd.journalq.broker.protocol.JournalqCommandHandler;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.message.SourceType;
import com.jd.journalq.network.command.AddConnectionRequest;
import com.jd.journalq.network.command.AddConnectionResponse;
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
 * AddConnectionRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/29
 */
public class AddConnectionRequestHandler implements JournalqCommandHandler, Type, BrokerContextAware {

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
            return BooleanAck.build(JournalqCode.CN_AUTHENTICATION_ERROR.getCode(),
                    JournalqCode.CN_AUTHENTICATION_ERROR.getMessage() + String.format(", app: %s", addConnectionRequest.getApp()));
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
        connection.setSource(SourceType.JMQ.name());
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
        return JournalqCommandType.ADD_CONNECTION_REQUEST.getCode();
    }
}