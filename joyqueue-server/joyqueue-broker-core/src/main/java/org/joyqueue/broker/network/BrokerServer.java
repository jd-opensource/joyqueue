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
package org.joyqueue.broker.network;

import com.google.common.base.Preconditions;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.monitor.SessionManager;
import org.joyqueue.broker.network.backend.BackendServer;
import org.joyqueue.broker.network.frontend.FrontendServer;
import org.joyqueue.broker.network.listener.BrokerTransportListener;
import org.joyqueue.broker.network.protocol.ProtocolManager;
import org.joyqueue.network.transport.config.ServerConfig;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Broker to broker communication
 *
 * author: gaohaoxiang
 * date: 2018/8/14
 */
public class BrokerServer extends Service {
    private static final Logger LOG= LoggerFactory.getLogger(BrokerServer.class);
    private FrontendServer frontendServer;
    private BackendServer backendServer;
    private BrokerTransportListener transportListener;
    private  ServerConfig frontendConfig;
    // broker to broker network
    private  ServerConfig backendConfig;
    public BrokerServer(BrokerContext brokerContext, ProtocolManager protocolManager) {
        Preconditions.checkArgument(brokerContext != null, "broker context can not be null");
        Preconditions.checkArgument(protocolManager != null, "protocol manager can not be null");
        SessionManager sessionManager = brokerContext.getSessionManager();

        frontendConfig = brokerContext.getBrokerConfig().getFrontendConfig();
        frontendConfig.setAcceptThreadName("joyqueue-frontend-accept-eventLoop");
        frontendConfig.setIoThreadName("joyqueue-frontend-io-eventLoop");

        backendConfig = brokerContext.getBrokerConfig().getBackendConfig();
        backendConfig.setAcceptThreadName("joyqueue-backend-accept-eventLoop");
        backendConfig.setIoThreadName("joyqueue-backend-io-eventLoop");

        this.transportListener = new BrokerTransportListener(sessionManager);
        this.frontendServer = new FrontendServer(frontendConfig, brokerContext, protocolManager);
        this.frontendServer.addListener(transportListener);
        this.backendServer = new BackendServer(backendConfig, brokerContext);
        this.backendServer.addListener(transportListener);
    }

    @Override
    protected void doStart() throws Exception {
        this.backendServer.start();
        this.frontendServer.start();
        LOG.info("Broker front/backend server on {}/{} started ",frontendConfig.getPort(),backendConfig.getPort());
    }

    @Override
    protected void doStop() {
        this.frontendServer.removeListener(transportListener);
        this.frontendServer.stop();
        this.backendServer.removeListener(transportListener);
        this.backendServer.stop();
    }
}