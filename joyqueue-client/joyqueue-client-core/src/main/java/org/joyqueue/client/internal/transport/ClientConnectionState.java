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
package org.joyqueue.client.internal.transport;

import org.joyqueue.client.internal.ClientConsts;
import org.joyqueue.client.internal.exception.ClientException;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.AddConnectionRequest;
import org.joyqueue.network.command.AddConnectionResponse;
import org.joyqueue.network.command.RemoveConnectionRequest;
import org.joyqueue.network.session.ClientId;
import org.joyqueue.network.session.Language;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.JoyQueueCommand;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.time.SystemClock;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * ClientConnectionState
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class ClientConnectionState {

    protected static final Logger logger = LoggerFactory.getLogger(ClientConnectionState.class);

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
        AddConnectionRequest addConnectionRequest = new AddConnectionRequest();
        ClientId clientId = new ClientId();

//        addConnection.setUsername(nameServerConfig.getUsername());
//        addConnection.setPassword(nameServerConfig.getPassword());

        // 如果app是app.group类型，那么分割为app
        // TODO group处理
        if (nameServerConfig.getApp().contains(".")) {
            addConnectionRequest.setApp(nameServerConfig.getApp().split("\\.")[0]);
        } else {
            addConnectionRequest.setApp(nameServerConfig.getApp());
        }
        addConnectionRequest.setToken(nameServerConfig.getToken());
        addConnectionRequest.setRegion(nameServerConfig.getRegion());
        addConnectionRequest.setNamespace(nameServerConfig.getNamespace());
        addConnectionRequest.setLanguage(Language.JAVA);
        clientId.setVersion(CLIENT_VERSION);
        clientId.setIp(CLIENT_IP);
        clientId.setTime(SystemClock.now());
        clientId.setSequence(SEQUENCE.incrementAndGet());
        addConnectionRequest.setClientId(clientId);

        try {
            Command response = client.sync(new JoyQueueCommand(addConnectionRequest));

            AddConnectionResponse addConnectionResponse = (AddConnectionResponse) response.getPayload();
            ClientConnectionInfo clientConnectionInfo = new ClientConnectionInfo();
            clientConnectionInfo.setConnectionId(addConnectionResponse.getConnectionId());
            handleNotification(addConnectionResponse);

            client.getAttribute().set(CONNECTION_INFO_KEY, clientConnectionInfo);
        } catch (ClientException e) {
            int code = e.getCode();
            String error = e.getMessage();
            if (code == JoyQueueCode.CN_AUTHENTICATION_ERROR.getCode()) {
                logger.error("client addConnection error, no permission, please check your app and token", error);
                throw e;
            } else {
                logger.error("client addConnection error, app: {}, token: {}, code: {}, error: {}", addConnectionRequest.getApp(), addConnectionRequest.getToken(), code, error);
                throw e;
            }
        }
    }

    protected void handleNotification(AddConnectionResponse addConnectionResponse) {
        if (StringUtils.isBlank(addConnectionResponse.getNotification())) {
            return;
        }
        doHandleNotification(addConnectionResponse);
    }

    protected void doHandleNotification(AddConnectionResponse addConnectionResponse) {
        logger.warn("{}", addConnectionResponse.getNotification());
    }

    public void handleDisconnection() {
        ClientConnectionInfo connectionInfo = getConnectionInfo();
        if (connectionInfo == null) {
            return;
        }

        RemoveConnectionRequest removeConnectionRequest = new RemoveConnectionRequest();

        try {
            Command response = client.sync(new JoyQueueCommand(removeConnectionRequest));
        } catch (Exception e) {
            logger.debug("client removeConnection error, connection: {}", removeConnectionRequest, e);
        } finally {
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