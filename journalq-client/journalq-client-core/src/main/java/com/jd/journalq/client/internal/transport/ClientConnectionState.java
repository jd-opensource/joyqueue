package com.jd.journalq.client.internal.transport;

import com.jd.journalq.client.internal.ClientConsts;
import com.jd.journalq.client.internal.exception.ClientException;
import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.network.command.AddConnection;
import com.jd.journalq.common.network.command.AddConnectionAck;
import com.jd.journalq.common.network.command.RemoveConnection;
import com.jd.journalq.common.network.session.ClientId;
import com.jd.journalq.common.network.session.Language;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.JMQCommand;
import com.jd.journalq.toolkit.network.IpUtil;
import com.jd.journalq.toolkit.time.SystemClock;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * ClientConnectionState
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class ClientConnectionState {

    protected static final Logger logger = LoggerFactory.getLogger(ClientConnectionState.class);

    private static final String ADDED_CONNECTION_KEY = "_CLIENT_ADDED_CONNECTION_";
    private static final String DISCONNECTED_KEY = "_CLIENT_DISCONNECTED_";
    private static final String CONNECTION_INFO_KEY = "_CLIENT_CONNECTION_INFO_";

    private static final AtomicLong SEQUENCE = new AtomicLong();
    private static final String CLIENT_IP = IpUtil.getLocalIp();
    private static final String CLIENT_VERSION = ClientConsts.VERSION;

    private NameServerConfig nameServerConfig;
    private Client client;
    private volatile long lastUseTime;

    public ClientConnectionState(NameServerConfig nameServerConfig, Client client) {
        this.nameServerConfig = nameServerConfig;
        this.client = client;
    }

    public void handleAddConnection() {
        if (client.getAttribute().contains(ADDED_CONNECTION_KEY)) {
            return;
        }
        doHandleAddConnection();
    }

    public void handleDisconnection() {
        if (client.getAttribute().contains(DISCONNECTED_KEY)) {
            return;
        }
        doHandleDisconnection();
    }

    protected void doHandleAddConnection() {
        AddConnection addConnection = new AddConnection();
        ClientId clientId = new ClientId();

//        addConnection.setUsername(nameServerConfig.getUsername());
//        addConnection.setPassword(nameServerConfig.getPassword());

        // 如果app是app.group类型，那么分割为app
        // TODO group处理
        if (nameServerConfig.getApp().contains(".")) {
            addConnection.setApp(nameServerConfig.getApp().split("\\.")[0]);
        } else {
            addConnection.setApp(nameServerConfig.getApp());
        }
        addConnection.setToken(nameServerConfig.getToken());
        addConnection.setRegion(nameServerConfig.getRegion());
        addConnection.setNamespace(nameServerConfig.getNamespace());
        addConnection.setLanguage(Language.JAVA);
        clientId.setVersion(CLIENT_VERSION);
        clientId.setIp(CLIENT_IP);
        clientId.setTime(SystemClock.now());
        clientId.setSequence(SEQUENCE.incrementAndGet());
        addConnection.setClientId(clientId);

        try {
            Command response = client.sync(new JMQCommand(addConnection));

            AddConnectionAck addConnectionAck = (AddConnectionAck) response.getPayload();
            ClientConnectionInfo clientConnectionInfo = new ClientConnectionInfo();
            clientConnectionInfo.setConnectionId(addConnectionAck.getConnectionId());
            handleNotification(addConnectionAck);

            client.getAttribute().set(CONNECTION_INFO_KEY, clientConnectionInfo);
            client.getAttribute().set(ADDED_CONNECTION_KEY, true);
        } catch (ClientException e) {
            int code = e.getCode();
            String error = e.getMessage();
            if (code == JMQCode.CN_AUTHENTICATION_ERROR.getCode()) {
                logger.error("client addConnection error, no permission, please check your app and token", error);
                throw e;
            } else {
                logger.error("client addConnection error, app: {}, token: {}, code: {}, error: {}", addConnection.getApp(), addConnection.getToken(), code, error);
                throw e;
            }
        }
    }

    protected void handleNotification(AddConnectionAck addConnectionAck) {
        if (StringUtils.isBlank(addConnectionAck.getNotification())) {
            return;
        }
        logger.warn("{}", addConnectionAck.getNotification());
    }

    protected void doHandleDisconnection() {
        ClientConnectionInfo connectionInfo = getConnectionInfo();
        if (connectionInfo == null) {
            return;
        }

        RemoveConnection removeConnection = new RemoveConnection();

        try {
            Command response = client.sync(new JMQCommand(removeConnection));
            client.getAttribute().set(DISCONNECTED_KEY, true);
        } catch (Exception e) {
            logger.debug("client removeConnection error, connection: {}", removeConnection, e);
        } finally {
            client.getAttribute().remove(ADDED_CONNECTION_KEY);
            client.getAttribute().remove(CONNECTION_INFO_KEY);
        }
    }

    public ClientConnectionInfo getConnectionInfo() {
        return client.getAttribute().get(CONNECTION_INFO_KEY);
    }

    public void updateUseTime() {
        lastUseTime = SystemClock.now();
    }

    public long getLastUseTime() {
        return lastUseTime;
    }
}